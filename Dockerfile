# syntax=docker/dockerfile:1

# ---------- Build stage: compile the SPA into static files ----------
FROM node:22-alpine AS build
WORKDIR /app

ARG PNPM_VERSION=10
RUN npm install --global pnpm@${PNPM_VERSION}

# Install dependencies first so this layer stays cached until the lockfile changes
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .

# Vite inlines VITE_* values into the bundle at build time, so they are build args
# (compose.yaml fills them from .env). They are public identifiers that end up in
# the browser: never put secrets here.
ARG VITE_AZURE_CLIENT_ID
ARG VITE_AZURE_REDIRECT_URI=http://localhost:5173
ARG VITE_API_SCOPE
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_AZURE_CLIENT_ID=${VITE_AZURE_CLIENT_ID} \
    VITE_AZURE_REDIRECT_URI=${VITE_AZURE_REDIRECT_URI} \
    VITE_API_SCOPE=${VITE_API_SCOPE} \
    VITE_API_BASE_URL=${VITE_API_BASE_URL}

# authConfig.ts only throws in the browser, so fail here instead of shipping a blank page
RUN test -n "$VITE_AZURE_CLIENT_ID" && test -n "$VITE_API_SCOPE" \
    || { echo "Build args VITE_AZURE_CLIENT_ID and VITE_API_SCOPE are required" >&2; exit 1; }

RUN pnpm build

# ---------- Runtime stage: nginx serving the static build ----------
FROM nginx:stable-alpine

COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/build/client /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1/healthz || exit 1
