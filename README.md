# ReservaFit

MVP académico para reservar horarios de entrenamiento en gimnasios de barrio independientes en Chile.

Monorepo:

- **Frontend** — React Router v8 (modo SPA) + Tailwind v4 + MSAL, en la raíz del repositorio.
- **Backend** — Spring Boot 4 como resource server (Java 25, H2 en memoria), en `ReservaFit-Backend/`.

La autenticación usa cuentas personales de Microsoft (MSAL). El frontend obtiene un access token y llama a la API con él.

## Requisitos

- Node 20+ y **npm**
- Java 25 (el backend incluye el wrapper de Maven)

## Variables de entorno

El frontend necesita un archivo `.env` en la raíz del repositorio (está en `.gitignore`):

```env
VITE_AZURE_CLIENT_ID=<client id del app registration del frontend>
VITE_AZURE_REDIRECT_URI=http://localhost:5173
VITE_API_SCOPE=api://<client id de la API>/access_as_user
```

Las tres son obligatorias: `app/authConfig.ts` lanza un error al importarse si falta alguna.

La configuración del backend vive en `ReservaFit-Backend/src/main/resources/application.yaml` (tenant, client id, audience). El tenant, el client id de la API y los orígenes CORS se pueden sobrescribir con variables de entorno (ver [Docker](#docker)); si no se definen, se usan los valores del `application.yaml`. El backend no lee el archivo `.env`.

## Ejecución

Frontend:

```bash
npm install
npm run dev        # http://localhost:5173
```

Backend:

```bash
cd ReservaFit-Backend
./mvnw spring-boot:run   # http://localhost:8080
./mvnw test
```

La base de datos H2 es en memoria y se puebla desde `data.sql`, así que las reservas se pierden en cada reinicio.

## Docker

Todo el stack (frontend + backend) se levanta con Docker Compose, sin instalar Node ni Java. Necesitas Docker Desktop (o Docker Engine con Compose v2).

1. Crea el archivo de variables: copia `.env.example` a `.env` y completa los valores. Si ya tienes un `.env` para `pnpm dev`, solo agrega `AZURE_API_CLIENT_ID`: las variables `VITE_*` son las mismas.
2. Construye y levanta los servicios:

   ```bash
   docker compose up --build
   ```

3. Abre http://localhost:5173.

| Servicio | URL | Imagen |
| --- | --- | --- |
| `frontend` | http://localhost:5173 | nginx sirviendo el build estático de la SPA (`Dockerfile`) |
| `backend` | http://localhost:8080 | Spring Boot sobre JRE 25 (`ReservaFit-Backend/Dockerfile`) |

El frontend se publica en el puerto 5173 a propósito: es el redirect URI que ya está registrado en Azure, así que no hay que cambiar nada allá. Por lo mismo, no lo corras a la vez que `pnpm dev`.

### Variables de entorno

Compose lee el archivo `.env` de la raíz del repositorio (ignorado por git). Ninguna variable es un secreto: son identificadores públicos de los app registrations.

| Variable | Dónde se usa | Obligatoria | Por defecto | Descripción |
| --- | --- | --- | --- | --- |
| `VITE_AZURE_CLIENT_ID` | frontend (build) | sí | — | Client ID del app registration ReservaFit-Frontend |
| `VITE_API_SCOPE` | frontend (build) | sí | — | `api://<client id de la API>/access_as_user` |
| `VITE_AZURE_REDIRECT_URI` | frontend (build) | no | `http://localhost:${FRONTEND_PORT}` | Debe estar registrado como redirect URI de tipo SPA en Azure |
| `VITE_API_BASE_URL` | frontend (build) | no | `http://localhost:${BACKEND_PORT}` | URL del backend vista desde el navegador |
| `AZURE_API_CLIENT_ID` | backend | sí | — | Client ID del app registration ReservaFit-API (audiencia de los tokens) |
| `AZURE_API_TENANT_ID` | backend | no | `9188040d-6c67-4c5b-b112-36a304b66dad` | Tenant "consumers" de las cuentas personales de Microsoft |
| `CORS_ALLOWED_ORIGINS` | backend | no | `http://localhost:${FRONTEND_PORT}` | Orígenes permitidos por CORS, separados por coma y sin espacios |
| `FRONTEND_PORT` | host | no | `5173` | Puerto donde se publica el frontend |
| `BACKEND_PORT` | host | no | `8080` | Puerto donde se publica el backend |

Notas:

- Las variables `VITE_*` se incrustan en el JavaScript al compilar. Si las cambias, reconstruye con `docker compose up --build`. Las del backend se leen al arrancar, así que basta `docker compose up -d`.
- Si cambias `FRONTEND_PORT`, registra `http://localhost:<puerto>` como redirect URI de tipo SPA en Azure.
- La base de datos sigue siendo H2 en memoria: al recrear el contenedor del backend se pierden las reservas.
- La primera construcción tarda unos minutos porque descarga las dependencias de Maven y pnpm.

Comandos útiles:

```bash
docker compose up -d --build --wait   # en segundo plano; espera a que ambos servicios estén healthy
docker compose ps                     # estado y healthchecks
docker compose logs -f backend        # logs del backend
docker compose down                   # detiene y elimina los contenedores
```

## Endpoints de la API

URL base: `http://localhost:8080`. Todo requiere un token `Bearer`, salvo el catálogo de gimnasios.

### Gimnasios — `/api/gimnasios` (público)

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/gimnasios?comuna=` | Lista gimnasios, con filtro opcional por comuna (sin distinguir mayúsculas) |
| GET | `/api/gimnasios/{id}` | Detalle del gimnasio (404 si no existe) |
| GET | `/api/gimnasios/{id}/horarios` | Horarios del gimnasio |
| GET | `/api/gimnasios/{id}/disponibilidad?fecha=YYYY-MM-DD` | Horarios con el indicador `available` para esa fecha |

### Usuarios — `/api/usuarios` (requiere token)

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/usuarios/me` | Registra o devuelve al usuario actual (201 si se creó, 200 si ya existía) |
| PUT | `/api/usuarios/me/perfil` | Completa el perfil (400 datos inválidos, 409 RUT duplicado) |

### Reservas — `/api/reservas` (requiere token)

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/reservas` | Crea una reserva (201; 400 fecha inválida, 404 horario inexistente, 409 horario ocupado) |
| GET | `/api/reservas/mias` | Reservas del usuario actual, primero las próximas |
| DELETE | `/api/reservas/{id}` | Cancela una reserva (409 si ya estaba cancelada o el horario ya comenzó) |

Las fechas viajan como strings `"YYYY-MM-DD"`. Los errores se devuelven como un mapa `campo → mensaje`.

## Estructura del proyecto

```
app/                    Rutas, componentes, lib y tipos de React Router
  lib/                  cliente de API, obtención del token, parseo de errores, utilidades de fechas
  routes/               layout, login, reservas, gimnasios/:id, mis-reservas
ReservaFit-Backend/
  src/main/java/...     controller, service, model, dto, config, exception
  src/main/resources/   application.yaml, data.sql (datos iniciales)
  Dockerfile            imagen del backend (Maven + JRE 25)
Dockerfile              imagen del frontend (build con pnpm + nginx)
compose.yaml            levanta frontend y backend juntos
docker/nginx.conf       configuración de nginx para la SPA
.env.example            plantilla de variables de entorno
```
