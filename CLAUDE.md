# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

ReservaFit is an academic MVP for booking slots at independent neighborhood gyms (Chile). UI copy is Spanish. It is a monorepo: a React Router SPA at the root and a Spring Boot API in `ReservaFit-Backend/`.

## Commands

Frontend (root, **pnpm** — not npm):

- `pnpm dev` — Vite dev server on `http://localhost:5173` (the user usually already has one running; don't start a second one, it will grab 5174)
- `pnpm typecheck` — `react-router typegen && tsc`; the main correctness check
- `pnpm build` — also worth running after auth changes: it exercises the SPA-mode prerender, which can break if browser-only code leaks into the route tree

There is no linter and no frontend test suite.

Backend (`cd ReservaFit-Backend`, Java 25, Maven wrapper):

- `./mvnw spring-boot:run` — API on `http://localhost:8080`
- `./mvnw compile` / `./mvnw test` (single test: `./mvnw test -Dtest=ClassName#method`)
- On Windows, stopping the Maven process does not always kill the forked JVM. If port 8080 is busy: `netstat -ano | grep :8080`, then `taskkill //PID <pid> //F`.

## Frontend architecture

- React Router v8 framework mode with `ssr: false` (SPA mode). Routes are declared in `app/routes.ts`: a `routes/layout.tsx` layout (shared footer) wrapping `/`, `/login` and a nested `routes/app-layout.tsx` layout for signed-in pages: `/reservas` (gym catalog), `/gimnasios/:id` (schedule and booking) and `/mis-reservas` (my bookings).
- Tailwind v4 via `@tailwindcss/vite`; design tokens live in `app/app.css`. The repo sits in OneDrive, which sometimes makes Tailwind miss newly created files. Re-saving `app/app.css` forces a rescan.

### Auth (MSAL, Microsoft personal accounts)

- `app/authConfig.ts` holds the MSAL config, reading `VITE_AZURE_CLIENT_ID`, `VITE_AZURE_REDIRECT_URI` and `VITE_API_SCOPE` from `.env` (typed in `app/vite-env.d.ts`). It throws at import time if any are missing. `.env` is gitignored and not readable by Claude; ask the user for variable names, not values.
- Authority is `https://login.microsoftonline.com/consumers`: the app registrations target personal Microsoft accounts.
- `app/entry.client.tsx` is a custom client entry. It creates and `initialize()`s the `PublicClientApplication` and wraps `<HydratedRouter/>` in `<MsalProvider>`. Keep MSAL here, out of `root.tsx`/route modules: it is the browser-only boundary.
- `loginRequest` asks for the API scope itself, not Graph, so consent and the API token come from login. Otherwise `acquireTokenSilent` falls back to a hidden iframe, which times out for personal accounts. `prompt: "select_account"` is set so users can switch accounts.
- Route guards: `routes/login.tsx` redirects to `/reservas` when authenticated, and `routes/app-layout.tsx` redirects to `/login` when not (or when the profile is incomplete), covering every signed-in page. Both wait for `inProgress === InteractionStatus.None` before deciding. The app layout also renders `AppNav` (NavLinks + logout menu), so pages under it render only their content.
- Logout (in `AppNav`) is local only: `instance.clearCache()`, followed by a hard `window.location.assign("/login")`. `clearCache()` emits no MSAL event, so `MsalProvider` state would otherwise stay stale. `logoutRedirect()` would also sign the user out of Microsoft everywhere.
- `app/lib/` splits API concerns: `api.ts` only holds the axios instance, the backend URL (`http://localhost:8080`, hardcoded) and one function per endpoint; `auth-token.ts` has `acquireApiToken` (`acquireTokenSilent`, with redirect fallback on `InteractionRequiredAuthError` or `timed_out`); `api-errors.ts` parses backend error bodies (`getFieldErrors`, `getErrorMessage`, `getErrorStatus`). `app/lib/use-current-user.ts` calls `POST /api/usuarios/me` and returns the profile. API data types live in `app/types/` (`user.ts`, `gym.ts`, `booking.ts`), not in `api.ts`.
- User registration: the first `POST /api/usuarios/me` creates the user from token claims. A profile is "complete" when RUT, birth date and card data are all set, not when the user is new. Incomplete profiles see `CompleteProfileForm` on `/login`, and `/reservas` sends them back there. Complete profiles go straight to `/reservas`.
- `/reservas` shows the gym catalog (`components/reservas/`: `ReservasPanel`, `GymCard`). It loads `GET /api/gimnasios` once via `fetchGyms` (no token) and filters by comuna client-side. "Ver horarios" links to `/gimnasios/:id` (`GymBookingPanel`: date picker, availability grid, `POST /api/reservas`). `/mis-reservas` (`MyBookingsPanel`) lists bookings and cancels with an inline two-step confirm.
- Dates travel as `"YYYY-MM-DD"` strings; use `app/lib/dates.ts` (local-date parsing/formatting). Never `new Date("YYYY-MM-DD")` or `toISOString()`: both are UTC and shift the day in Chile.

## Backend architecture

- Spring Boot **4.0.8**. Do not bump to 4.1.x without checking Spring Cloud Azure support. `spring-cloud-azure-starter-active-directory` has no version in `pom.xml`: it comes from the imported `spring-cloud-azure-dependencies` BOM (7.4.0). Don't pin the starter directly.
- Resource server only: `config/SecurityConfig.java` requires a JWT on every request except `GET /api/gimnasios` and `/api/gimnasios/**` (the gym catalog is public), plus a CORS bean allowing `http://localhost:5173`. The Azure starter auto-configures JWT validation (issuer, audience) from `application.yaml` (`spring.cloud.azure.active-directory.*`).
- `profile.tenant-id` must be the consumers tenant GUID `9188040d-6c67-4c5b-b112-36a304b66dad`. The starter rejects the `consumers` alias for resource servers. Tokens from the org tenant `569ca4b8-...` (such as client-credentials tokens) are correctly rejected with 401.
- JPA entities in `model/` (`Usuario`, `Gimnasio`, `Horario`, `Reserva`, `EstadoReserva`) use Lombok and an in-memory H2 database, so all data is lost on restart. Entity fields and endpoint paths are Spanish (`/api/gimnasios`, `/api/usuarios`, `/api/reservas`); all other backend code (classes, methods, JSON fields) is English. No Java records: DTOs are Lombok classes (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor`).
- Users: `UserController` exposes `POST /api/usuarios/me` (201 when created, 200 when it already existed) and `PUT /api/usuarios/me/perfil` (400 on invalid data, 409 on a duplicate RUT; the RUT's format is validated, not its check digit). `UserService` keys users by the token's `oid` (`Usuario.microsoftOid`). `ApiExceptionHandler` returns error bodies as field → message maps. Custom exceptions live in the `exception/` package (never in `service/`, which holds only services) and are mapped to status codes in `ApiExceptionHandler`. `controller/TestAuthController.java` (`GET /api/home`) is a leftover placeholder.
- Gyms: `GymController` exposes public `GET /api/gimnasios` (optional `?comuna=`, case-insensitive, sorted by name), `GET /api/gimnasios/{id}` and `GET /api/gimnasios/{id}/horarios` (both 404 when the gym is missing). `Gimnasio.imagenUrl` (JSON `imageUrl`) is a plain URL; seed images are hotlinked from Unsplash. Seed gyms and schedules live in `src/main/resources/data.sql`, which needs `spring.jpa.defer-datasource-initialization: true` because Hibernate generates the schema. `GET /api/gimnasios/{id}/disponibilidad?fecha=YYYY-MM-DD` (public) returns each horario with `available`.
- Bookings: `BookingController` exposes `POST /api/reservas` (201; 400 past date, >30 days ahead or already-started slot today; 404 unknown horario; 409 slot taken), `GET /api/reservas/mias` (upcoming confirmed first, ascending; then past/canceled, descending) and `DELETE /api/reservas/{id}` (soft cancel, 200 with the booking; 404 also for another user's booking; 409 already canceled or slot already started). A confirmed booking can be canceled until its slot starts (`BookingService.hasStarted`, which also drives the `cancellable` JSON field). `Reserva.fechaCreacion` uses `@CreationTimestamp`. `Reserva.activa` is TRUE while confirmed and NULL when canceled, and `uq_reserva_slot` is UNIQUE(gimnasio_id, horario_id, fecha, activa): NULLs are distinct, so canceled rows never block re-booking. Never set `activa` to FALSE. "Today"/"now" come from the `Clock` bean (`config/ClockConfig`, America/Santiago); use `LocalDate.now(clock)` so tests can use a fixed clock. `BookingService` maps to DTOs inside its transactions (associations are LAZY).

## Azure app registrations

- **ReservaFit-Frontend**: client ID `8b56e001-8f57-4bd1-9329-ffdea5aeeae6`, SPA redirect URI `http://localhost:5173`.
- **ReservaFit-API**: client ID `f4a5103b-ae57-406d-85e1-eee64edbc4ac`, exposes `api://f4a5103b-ae57-406d-85e1-eee64edbc4ac/access_as_user`.

To test the API from Postman, either copy the `Bearer` token from the browser's Network tab, or add a "Mobile and desktop" redirect URI (`https://oauth.pstmn.io/v1/callback`) to the frontend registration. Postman can't redeem codes for the SPA redirect URI (`AADSTS90023`).
