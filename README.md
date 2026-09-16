# ReservaFit

MVP académico para reservar horarios de entrenamiento en gimnasios de barrio independientes en Chile.

Monorepo:

- **Frontend** — React Router v8 (modo SPA) + Tailwind v4 + MSAL, en la raíz del repositorio.
- **Backend** — Spring Boot 4 como resource server (Java 25, H2 en memoria), en `ReservaFit-Backend/`.

La autenticación usa cuentas personales de Microsoft (MSAL). El frontend obtiene un access token y llama a la API con él.

## Requisitos

- Node 20+ y **pnpm**
- Java 25 (el backend incluye el wrapper de Maven)

## Variables de entorno

El frontend necesita un archivo `.env` en la raíz del repositorio (está en `.gitignore`):

```env
VITE_AZURE_CLIENT_ID=<client id del app registration del frontend>
VITE_AZURE_REDIRECT_URI=http://localhost:5173
VITE_API_SCOPE=api://<client id de la API>/access_as_user
```

Las tres son obligatorias: `app/authConfig.ts` lanza un error al importarse si falta alguna.

La configuración del backend vive en `ReservaFit-Backend/src/main/resources/application.yaml` (tenant, client id, audience). Ahí no se usa `.env`.

## Ejecución

Frontend:

```bash
pnpm install
pnpm dev        # http://localhost:5173
pnpm typecheck  # chequeo principal de correctitud
pnpm build
```

Backend:

```bash
cd ReservaFit-Backend
./mvnw spring-boot:run   # http://localhost:8080
./mvnw test
```

La base de datos H2 es en memoria y se puebla desde `data.sql`, así que las reservas se pierden en cada reinicio.

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
```

El detalle de convenciones y arquitectura está en `CLAUDE.md`.
