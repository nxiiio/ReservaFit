#!/usr/bin/env bash
# Builds the six production images, pushes them to ECR and tells the EC2 instance to
# pull and restart them. Run it from the repo root in Git Bash (Windows) or any bash:
#
#   bash scripts/build-push.sh
#
# Needs: the lab credentials in ~/.aws/credentials, Docker running, `terraform apply`
# already done in infra/ (it reads the registry, URL and instance from its outputs), and
# VITE_AZURE_CLIENT_ID and VITE_API_SCOPE in the root .env (the same ones pnpm dev uses).
# Set SKIP_RESTART=1 to only push.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REGION=us-east-1
# Same list as infra/ecr.tf; the backend modules are in the order compose starts them
BACKEND_MODULES=(ms-usuarios ms-gimnasios ms-notificaciones ms-reservas api-gateway)

# Reads KEY from the root .env, unless it is already set in the environment.
# tr drops the \r that Windows editors leave at the end of each line.
env_value() {
  local key="$1"
  if [[ -n "${!key:-}" ]]; then
    printf '%s' "${!key}"
  elif [[ -f "$ROOT/.env" ]]; then
    grep -E "^${key}=" "$ROOT/.env" | tail -n 1 | cut -d= -f2- | tr -d '\r'
  fi
}

VITE_AZURE_CLIENT_ID="$(env_value VITE_AZURE_CLIENT_ID)"
VITE_API_SCOPE="$(env_value VITE_API_SCOPE)"
if [[ -z "$VITE_AZURE_CLIENT_ID" || -z "$VITE_API_SCOPE" ]]; then
  echo "VITE_AZURE_CLIENT_ID and VITE_API_SCOPE must be set in .env or in the environment" >&2
  exit 1
fi

cd "$ROOT/infra"
REGISTRY="$(terraform output -raw registry)"
URL="$(terraform output -raw url)"
INSTANCE_ID="$(terraform output -raw instance_id)"
cd "$ROOT"

# Every image gets latest (what the instance pulls) and the commit, to tell builds apart in ECR
TAG="$(git rev-parse --short HEAD)"

echo "==> Logging in to $REGISTRY"
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRY"

build_and_push() {
  local name="$1"
  shift
  local image="$REGISTRY/reservafit/$name"
  echo "==> Building $name"
  # The instance is x86_64 (t3); --platform keeps that true when building on an ARM machine
  docker build --platform linux/amd64 -t "$image:latest" -t "$image:$TAG" "$@"
  echo "==> Pushing $name"
  docker push "$image:latest"
  docker push "$image:$TAG"
}

# Vite inlines these into the bundle: the SPA calls the API on its own origin, and Caddy
# sends /api/* to the gateway
build_and_push frontend \
  --build-arg VITE_AZURE_CLIENT_ID="$VITE_AZURE_CLIENT_ID" \
  --build-arg VITE_API_SCOPE="$VITE_API_SCOPE" \
  --build-arg VITE_AZURE_REDIRECT_URI="$URL" \
  --build-arg VITE_API_BASE_URL="$URL" \
  .

for module in "${BACKEND_MODULES[@]}"; do
  build_and_push "$module" --build-arg MODULE="$module" ReservaFit-Backend
done

if [[ "${SKIP_RESTART:-}" == "1" ]]; then
  echo "==> Pushed $TAG. Skipping the restart (SKIP_RESTART=1)"
  exit 0
fi

# reservafit.service runs `docker compose up -d --pull always`; --no-block returns at once
# instead of waiting for every healthcheck
echo "==> Restarting the stack on $INSTANCE_ID"
if aws ssm send-command \
  --region "$REGION" \
  --instance-ids "$INSTANCE_ID" \
  --document-name AWS-RunShellScript \
  --comment "ReservaFit deploy $TAG" \
  --parameters 'commands=["systemctl restart --no-block reservafit"]' \
  --query Command.CommandId --output text; then
  echo "==> Done. The new version is live in a few minutes at $URL"
else
  # Right after the first apply the SSM agent may not be registered yet. The first start
  # needs no restart: reservafit.service retries every 30 s until the images exist.
  echo "==> Images pushed, but Session Manager could not reach $INSTANCE_ID." >&2
  echo "    On the first deploy the instance picks them up on its own; otherwise run" >&2
  echo "    'sudo systemctl restart reservafit' from Session Manager." >&2
fi
