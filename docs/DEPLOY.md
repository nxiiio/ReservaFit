# Despliegue en AWS (Learner Lab)

Despliega ReservaFit en una cuenta de **AWS Academy Learner Lab** con Terraform. Todo corre en una sola EC2:

```
Navegador ──HTTPS──▶ Caddy (80/443, certificado de Let's Encrypt)
                       ├─ /api/*  ──▶ api-gateway ──▶ ms-usuarios / ms-gimnasios / ms-reservas / ms-notificaciones
                       └─ /*      ──▶ frontend (nginx con la SPA)
                     MySQL y RabbitMQ en la misma red de Docker, sin puertos publicados
```

| Pieza | Dónde |
|---|---|
| Infraestructura (EC2, Elastic IP, Security Group, repositorios ECR) | `infra/` |
| Stack de producción | `compose.prod.yaml` y `docker/Caddyfile` (cloud-init los copia a `/opt/reservafit` en la EC2) |
| Build y push de las imágenes | `scripts/build-push.sh` |

Por qué este diseño:

- **HTTPS es obligatorio.** Entra ID solo acepta redirect URIs `http://` para `localhost`, y una página HTTPS no puede llamar a un API HTTP. El lab no permite CloudFront ni registrar dominios, así que el certificado lo saca Caddy para un subdominio gratis de DuckDNS.
- **La SPA y el API comparten origen.** Caddy enruta `/api/*` al gateway, así que `VITE_API_BASE_URL` y `CORS_ALLOWED_ORIGINS` son la misma URL.
- **MySQL y RabbitMQ van en contenedores**, no en RDS ni Amazon MQ. El lab no detiene RDS al terminar la sesión, así que gastaría créditos 24/7.
- **No hay SSH.** La EC2 usa `LabInstanceProfile`, y se entra por Session Manager.

## Antes de empezar (una vez)

1. **Herramientas** en tu PC: Terraform ≥ 1.10, AWS CLI v2, Docker Desktop y Git Bash.

   ```bash
   winget install Hashicorp.Terraform
   ```

   ```bash
   winget install Amazon.AWSCLI
   ```

2. **Saca `.terraform/` de OneDrive.** El provider de AWS pesa cientos de MB. En PowerShell (luego abre una terminal nueva):

   ```powershell
   [Environment]::SetEnvironmentVariable("TF_DATA_DIR", "C:\tf\reservafit", "User")
   ```

3. **Subdominio en DuckDNS.** Entra a <https://www.duckdns.org> (con GitHub o Google) y crea uno, por ejemplo `reservafit` → `reservafit.duckdns.org`. La IP se pone después del `apply`.

4. **Azure.** En la app registration **ReservaFit-Frontend**, ve a *Authentication* → *Single-page application* y agrega `https://reservafit.duckdns.org` (tu subdominio). No borres `http://localhost:5173`.

5. **Bucket para el state de Terraform** (con las credenciales del lab ya configuradas, ver abajo):

   ```bash
   ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
   aws s3 mb "s3://reservafit-tfstate-$ACCOUNT_ID" --region us-east-1
   aws s3api put-bucket-versioning --bucket "reservafit-tfstate-$ACCOUNT_ID" --versioning-configuration Status=Enabled
   ```

6. **Valores locales** (ambos archivos están en `.gitignore`, nunca se suben):

   ```bash
   cd infra
   cp backend.hcl.example backend.hcl            # reemplaza ACCOUNT_ID
   cp terraform.tfvars.example terraform.tfvars  # tu dominio y dos contraseñas
   openssl rand -hex 24                          # una vez por contraseña
   ```

## En cada sesión del lab

Las credenciales vencen con la sesión. Haz *Start Lab*, luego *AWS Details* → *AWS CLI* → *Show*, y pega el bloque en `~/.aws/credentials` (en Windows, `C:\Users\<tú>\.aws\credentials`). Comprueba que funcionan:

```bash
aws sts get-caller-identity
```

## Desplegar

```bash
cd infra
terraform init -backend-config=backend.hcl
terraform plan
terraform apply
```

Después del primer `init`, commitea `infra/.terraform.lock.hcl`: fija la versión exacta del provider.

1. **DNS.** Copia la IP de `terraform output public_ip` en DuckDNS, en el campo *current ip* de tu subdominio, y presiona *update ip*. La Elastic IP no cambia entre sesiones, así que esto se hace una sola vez (hasta un `terraform destroy`). Para verificar: `nslookup reservafit.duckdns.org`.

2. **Imágenes.** Desde la raíz del repo, en Git Bash y con Docker Desktop abierto:

   ```bash
   bash scripts/build-push.sh
   ```

   El script toma `VITE_AZURE_CLIENT_ID` y `VITE_API_SCOPE` de tu `.env`, construye las 6 imágenes con la URL de producción, las sube a ECR y le pide a la EC2 que se actualice. La primera vez tarda (Maven baja todas las dependencias).

3. **Espera unos 5 minutos** y abre `terraform output url`. En el primer arranque, la EC2 instala Docker, reintenta cada 30 s hasta que las imágenes existan en ECR, levanta los servicios en orden y Caddy pide el certificado.

**Prueba de humo:** iniciar sesión, completar el perfil, reservar, ver la notificación en la campana y cancelar. La base parte vacía, salvo los gimnasios de `data.sql`.

## Actualizar

Commitea tus cambios y vuelve a correr `bash scripts/build-push.sh`. Cada imagen queda con el tag `latest` y con el hash del commit.

Si cambias `compose.prod.yaml`, `docker/Caddyfile`, `infra/cloud-init.yaml.tftpl` o una variable de `terraform.tfvars`, corre `terraform apply`. Eso **reemplaza la EC2 y borra la base de datos**: el plan lo muestra como `must be replaced`. Si te importan los datos, haz un respaldo antes (ver abajo).

## Ver qué pasa en la EC2

En la consola, *EC2* → la instancia `reservafit` → *Connect* → *Session Manager*:

```bash
sudo -i
cd /opt/reservafit
docker compose ps                  # estado y healthchecks
docker compose logs -f caddy       # certificado, peticiones
docker compose logs -f ms-reservas
journalctl -u reservafit           # arranques del stack
cat /var/log/cloud-init-output.log # instalación del primer arranque
docker compose exec rabbitmq rabbitmqctl list_queues name messages
```

Respaldo y restauración de MySQL:

```bash
docker compose exec mysql sh -c 'mysqldump -ureservafit -p"$MYSQL_PASSWORD" reservafit_db' > /root/backup.sql
docker compose exec -T mysql sh -c 'mysql -ureservafit -p"$MYSQL_PASSWORD" reservafit_db' < /root/backup.sql
```

## Cuidar el presupuesto

Si te pasas del presupuesto del lab, **la cuenta se desactiva y se pierde todo**. El saldo que muestra el lab se actualiza cada 8–12 h.

| Situación | Qué hacer | Costo aprox. |
|---|---|---|
| Terminaste por hoy | Nada: el lab detiene la EC2 al terminar la sesión | disco (~$2,4/mes) y Elastic IP (~$3,6/mes) |
| Vas a entrar al lab pero no necesitas la app | El lab **vuelve a encender la EC2 al iniciar la sesión**: detenla con `aws ec2 stop-instances --instance-ids $(terraform output -raw instance_id)` | igual que arriba |
| No la usarás por días | `terraform destroy` | $0 |

Con la t3.medium encendida se gastan unos $0,04 por hora. `terraform destroy` borra todo, incluidos los datos y las imágenes de ECR (no el bucket del state). Además libera la Elastic IP: al volver a desplegar hay que poner la nueva IP en DuckDNS y repetir `build-push.sh`.

## Problemas comunes

| Síntoma | Causa |
|---|---|
| `ExpiredToken` o `InvalidClientTokenId` | Las credenciales del lab vencieron: vuelve a copiarlas |
| `AccessDenied` al crear algo de IAM | El lab no permite crear roles; este stack solo usa `LabInstanceProfile` |
| El navegador avisa que el certificado no es válido, o Caddy registra errores de ACME | El dominio no apunta a la Elastic IP (revisa DuckDNS) o el puerto 80 está cerrado |
| `502` justo después de desplegar | Los servicios siguen arrancando: `docker compose ps` |
| `AADSTS50011` al iniciar sesión | Falta registrar `https://<tu-dominio>` como SPA redirect URI en Azure |
| CORS bloquea las llamadas al API | La imagen del frontend se construyó con otra URL: vuelve a correr `build-push.sh` |

## Seguridad

- `terraform.tfvars`, `backend.hcl` y el state (en S3) tienen las contraseñas: no se commitean. `.dockerignore` deja `infra/` fuera de la imagen del frontend.
- Las contraseñas también viajan en el user data de la EC2, que puede leer cualquier usuario de la consola de la cuenta. Es aceptable para un MVP en una cuenta personal; la alternativa sería Secrets Manager.
- Desde internet solo se alcanzan los puertos 80 y 443 (Caddy). El panel de RabbitMQ y MySQL no están expuestos.
