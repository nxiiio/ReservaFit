# ReservaFit

MVP académico para reservar horarios de entrenamiento en gimnasios de barrio independientes en Chile.

Monorepo:

- **Frontend** — React Router v8 (modo SPA) + Tailwind v4 + MSAL, en la raíz del repositorio.
- **Backend** — Microservicios Spring Boot 4 (Java 25) detrás de un API Gateway, con una base MySQL compartida, en `ReservaFit-Backend/`.

La autenticación usa cuentas personales de Microsoft (MSAL). El frontend obtiene un access token y llama a la API con él.

## Requisitos

- Node 20+ y **npm**
- Java 25 (el backend incluye el wrapper de Maven)
- Docker, para levantar MySQL y RabbitMQ

## Variables de entorno

El frontend necesita un archivo `.env` en la raíz del repositorio (está en `.gitignore`):

```env
VITE_AZURE_CLIENT_ID=<client id del app registration del frontend>
VITE_AZURE_REDIRECT_URI=http://localhost:5173
VITE_API_SCOPE=api://<client id de la API>/access_as_user
```

Las tres son obligatorias: `app/authConfig.ts` lanza un error al importarse si falta alguna.

La configuración del backend vive en el `src/main/resources/application.yaml` de cada módulo (puerto, base de datos, y tenant, client id y audience de Azure). El tenant, el client id de la API, los orígenes CORS y la conexión a MySQL se pueden sobrescribir con variables de entorno (ver [Docker](#docker)); si no se definen, se usan los valores del `application.yaml`. El backend no lee el archivo `.env`.

## Ejecución

Frontend:

```bash
npm install
npm run dev        # http://localhost:5173
```

Backend (cada servicio en su propia terminal):

```bash
docker compose up -d mysql rabbitmq     # MySQL (localhost:3306) y RabbitMQ (localhost:5672), desde la raíz del repo
cd ReservaFit-Backend
./mvnw -pl api-gateway spring-boot:run  # http://localhost:8080, la URL que usa el frontend
./mvnw -pl ms-gimnasios spring-boot:run      # :8082 (antes que ms-reservas)
./mvnw -pl ms-notificaciones spring-boot:run # :8084 (antes que ms-reservas)
./mvnw -pl ms-usuarios spring-boot:run       # :8081
./mvnw -pl ms-reservas spring-boot:run       # :8083
./mvnw test                             # tests de ms-reservas, con H2 en memoria
```

| Servicio | Puerto | Rutas | Tablas |
| --- | --- | --- | --- |
| `api-gateway` | 8080 | Reenvía cada request según la ruta | (ninguna) |
| `ms-usuarios` | 8081 | `/api/usuarios/**` | `usuario` |
| `ms-gimnasios` | 8082 | `/api/gimnasios/**` | `gimnasio`, `horario`, `reserva_gimnasio` |
| `ms-reservas` | 8083 | `/api/reservas/**` y `/api/gimnasios/{id}/disponibilidad` | `reserva` |
| `ms-notificaciones` | 8084 | `/api/notificaciones/**` | `notificacion` |
| `rabbitmq` | 5672, panel en 15672 | Mensajes entre los servicios | (ninguna) |

Los datos quedan guardados entre reinicios (`docker compose down -v` borra la base). `ms-gimnasios` carga los gimnasios desde su `data.sql` sin duplicarlos.

Ningún servicio lee tablas de otro. `ms-reservas` guarda solo reservas: cada una tiene el `oid` del usuario, que viene en su token de Microsoft, así que no necesita datos de usuarios. Los horarios se los pregunta a `ms-gimnasios` por HTTP con OpenFeign (`client/GymClient`, directo a `GIMNASIOS_URL`, sin pasar por el gateway). Si `ms-gimnasios` no responde, reservar y ver la disponibilidad devuelven 503.

### RabbitMQ

| Mensaje | Lo publica | Cuándo | Cola | Lo consume |
| --- | --- | --- | --- | --- |
| `booking.created` | `ms-reservas` | Al crear una reserva (`POST /api/reservas`) | `gimnasios.bookings` y `notificaciones.bookings` | `ms-gimnasios` → `reserva_gimnasio`; `ms-notificaciones` → `notificacion` |
| `booking.cancelled` | `ms-reservas` | Al cancelar una reserva (`DELETE /api/reservas/{id}`) | las mismas dos | los mismos dos |

- Los mensajes van al exchange topic `reservafit.events`. Las colas `gimnasios.bookings` y `notificaciones.bookings` tienen el mismo binding `booking.*`, así que cada una recibe su propia copia de cada mensaje: `ms-reservas` publica una vez y no sabe cuántos servicios lo escuchan.
- `ms-notificaciones` convierte cada mensaje en una notificación para el usuario ("Reservaste en Fuerza Barrio Gym el martes 21 de octubre a las 18:00"). La página la muestra en la campana de la barra de navegación, que consulta cada 30 segundos y también justo después de reservar o cancelar. Un mensaje repetido no duplica la notificación. Si `ms-notificaciones` está apagado, reservar sigue funcionando y los mensajes esperan en su cola hasta que vuelve.
- Las reservas se envían recién cuando quedan guardadas en la base de datos (después del commit), así que una reserva rechazada no genera mensaje. `ms-gimnasios` guarda cada una en `reserva_gimnasio` (con el mismo id de la reserva, así que la cancelación reemplaza la fila) y lo deja en su log junto con cuántas reservas confirmadas tiene el gimnasio.
- Si el servicio que consume falla al procesar un mensaje, lo reintenta 3 veces (a 1, 2 y 4 segundos). Si sigue fallando, lo escribe en el log y RabbitMQ lo mueve a la DLQ de su cola (`gimnasios.bookings.dlq` o `notificaciones.bookings.dlq`) a través del exchange `reservafit.dlx`, donde queda guardado. Por ejemplo, una reserva para un gimnasio que no existe termina en `gimnasios.bookings.dlq`.
- Si un mensaje no calza con ninguna cola, o RabbitMQ no lo confirma, `ms-reservas` lo escribe en su log.
- Los logs salen por consola y en `logs/<servicio>.log`.
- El panel de RabbitMQ está en http://localhost:15672 (usuario y clave `reservafit`). Ahí se ven las colas, las DLQ y se pueden publicar mensajes de prueba.
- Cada servicio tiene su propio paquete `messaging/` con sus nombres (`RabbitNames`), sus clases de mensaje y su configuración (`RabbitConfig`). Los servicios no comparten código: el acuerdo entre ellos son los nombres de las colas y los campos del JSON, así que un cambio en un mensaje hay que hacerlo en `ms-reservas`, `ms-gimnasios` y `ms-notificaciones`.
- Cada cola la crea el servicio que la consume. Al levantar con `./mvnw`, arranca `ms-gimnasios` y `ms-notificaciones` antes que `ms-reservas` (que además le pide los horarios a `ms-gimnasios`). `compose.yaml` ya respeta ese orden.

## Docker

Todo el stack (MySQL, RabbitMQ, los cinco servicios del backend y el frontend) se levanta con Docker Compose, sin instalar Node ni Java. Necesitas Docker Desktop (o Docker Engine con Compose v2).

1. Crea el archivo de variables: copia `.env.example` a `.env` y completa los valores. Si ya tienes un `.env` para `pnpm dev`, sirve tal cual: las variables `VITE_*` son las mismas.
2. Construye y levanta los servicios:

   ```bash
   docker compose up --build
   ```

3. Abre http://localhost:5173.

| Servicio | URL | Imagen |
| --- | --- | --- |
| `frontend` | http://localhost:5173 | nginx sirviendo el build estático de la SPA (`Dockerfile`) |
| `api-gateway` | http://localhost:8080 | Spring Boot sobre JRE 25 (`ReservaFit-Backend/Dockerfile`, `MODULE=api-gateway`) |
| `ms-usuarios`, `ms-gimnasios`, `ms-reservas`, `ms-notificaciones` | solo dentro de la red de Compose | Misma imagen base, un `MODULE` distinto cada uno |
| `mysql` | `localhost:3306` | MySQL 8.4, con los datos en el volumen `mysql-data` |
| `rabbitmq` | `localhost:5672`, panel en http://localhost:15672 | RabbitMQ 4 con el plugin de administración |

Los microservicios no se publican en el host: el navegador solo habla con el gateway, que los encuentra por nombre (`http://ms-reservas:8083`, etc.). El frontend se publica en el puerto 5173 a propósito: es el redirect URI que ya está registrado en Azure, así que no hay que cambiar nada allá. Por lo mismo, no lo corras a la vez que `pnpm dev`.

### Variables de entorno

Compose lee el archivo `.env` de la raíz del repositorio (ignorado por git). Ninguna variable es un secreto: son identificadores públicos de los app registrations.

| Variable | Dónde se usa | Obligatoria | Por defecto | Descripción |
| --- | --- | --- | --- | --- |
| `VITE_AZURE_CLIENT_ID` | frontend (build) | sí | — | Client ID del app registration ReservaFit-Frontend |
| `VITE_API_SCOPE` | frontend (build) | sí | — | `api://<client id de la API>/access_as_user` |
| `VITE_AZURE_REDIRECT_URI` | frontend (build) | no | `http://localhost:${FRONTEND_PORT}` | Debe estar registrado como redirect URI de tipo SPA en Azure |
| `VITE_API_BASE_URL` | frontend (build) | no | `http://localhost:${BACKEND_PORT}` | URL del backend vista desde el navegador |
| `AZURE_API_CLIENT_ID` | microservicios | no | `f4a5103b-ae57-406d-85e1-eee64edbc4ac` | Client ID del app registration ReservaFit-API (audiencia de los tokens) |
| `AZURE_API_TENANT_ID` | microservicios | no | `9188040d-6c67-4c5b-b112-36a304b66dad` | Tenant "consumers" de las cuentas personales de Microsoft |
| `CORS_ALLOWED_ORIGINS` | gateway y microservicios | no | `http://localhost:${FRONTEND_PORT}` | Orígenes permitidos por CORS, separados por coma y sin espacios |
| `FRONTEND_PORT` | host | no | `5173` | Puerto donde se publica el frontend |
| `BACKEND_PORT` | host | no | `8080` | Puerto donde se publica el gateway |

Notas:

- Las variables `VITE_*` se incrustan en el JavaScript al compilar. Si las cambias, reconstruye con `docker compose up --build`. Las del backend se leen al arrancar, así que basta `docker compose up -d`.
- Si cambias `FRONTEND_PORT`, registra `http://localhost:<puerto>` como redirect URI de tipo SPA en Azure.
- Los datos de MySQL y los mensajes de RabbitMQ se guardan en los volúmenes `mysql-data` y `rabbitmq-data`, así que sobreviven a `docker compose down`. `docker compose down -v` los borra.
- La primera construcción tarda unos minutos porque descarga las dependencias de Maven y pnpm.

Comandos útiles:

```bash
docker compose up -d --build --wait   # en segundo plano; espera a que todo esté healthy
docker compose ps                     # estado y healthchecks
docker compose logs -f ms-reservas    # logs de un servicio
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
| GET | `/api/gimnasios/horarios/{id}` | Un horario con los datos de su gimnasio (lo usa `ms-reservas`; 404 si no existe) |
| GET | `/api/gimnasios/{id}/disponibilidad?fecha=YYYY-MM-DD` | Horarios con el indicador `available` para esa fecha |

### Usuarios — `/api/usuarios` (requiere token)

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/usuarios/me` | Registra o devuelve al usuario actual (201 si se creó, 200 si ya existía) |
| PUT | `/api/usuarios/me/perfil` | Completa el perfil (400 datos inválidos, 409 RUT duplicado) |

### Reservas — `/api/reservas` (requiere token)

| Método | Ruta | Descripción |
| --- | --- | --- |
| POST | `/api/reservas` | Crea una reserva (201; 400 fecha inválida, 404 horario inexistente, 409 horario ocupado, 503 si `ms-gimnasios` no responde) |
| GET | `/api/reservas/mias` | Reservas del usuario actual, primero las próximas |
| DELETE | `/api/reservas/{id}` | Cancela una reserva (409 si ya estaba cancelada o el horario ya comenzó) |

### Notificaciones — `/api/notificaciones` (requiere token)

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/notificaciones` | Notificaciones del usuario actual, las más nuevas primero |
| PUT | `/api/notificaciones/{id}/leida` | Marca una como leída (404 si no es del usuario) |
| PUT | `/api/notificaciones/leidas` | Marca todas como leídas (204) |

Las fechas viajan como strings `"YYYY-MM-DD"`. Los errores se devuelven como un mapa `campo → mensaje`.

## Estructura del proyecto

```
app/                    Rutas, componentes, lib y tipos de React Router
  lib/                  cliente de API, obtención del token, parseo de errores, utilidades de fechas
  routes/               layout, login, reservas, gimnasios/:id, mis-reservas
ReservaFit-Backend/     proyecto Maven multimódulo (pom.xml padre)
  api-gateway/          rutas en application.yaml, CORS en config/
  ms-usuarios/          controller, service, model, dto, config, exception
  ms-gimnasios/         ídem, más data.sql (datos iniciales)
  ms-reservas/          ídem, más messaging, client (OpenFeign) y tests
  ms-notificaciones/    ídem, más messaging (consume las reservas)
  Dockerfile            imagen de cualquier módulo (build arg MODULE; Maven + JRE 25)
Dockerfile              imagen del frontend (build con pnpm + nginx)
compose.yaml            levanta MySQL, RabbitMQ, los microservicios y el frontend
docker/nginx.conf       configuración de nginx para la SPA
.env.example            plantilla de variables de entorno
```
