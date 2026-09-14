# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

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

`Dockerfile` is the React Router template default: it uses `npm ci` and `package-lock.json`, which this repo doesn't have. It won't build as is.

## Frontend architecture

- React Router v8 framework mode with `ssr: false` (SPA mode). Routes are declared in `app/routes.ts`: a `routes/layout.tsx` layout (shared footer) wrapping `/`, `/login` and `/reservas`.
- Tailwind v4 via `@tailwindcss/vite`; design tokens live in `app/app.css`. The repo sits in OneDrive, which sometimes makes Tailwind miss newly created files. Re-saving `app/app.css` forces a rescan.

### Auth (MSAL, Microsoft personal accounts)

- `app/authConfig.ts` holds the MSAL config, reading `VITE_AZURE_CLIENT_ID`, `VITE_AZURE_REDIRECT_URI` and `VITE_API_SCOPE` from `.env` (typed in `app/vite-env.d.ts`). It throws at import time if any are missing. `.env` is gitignored and not readable by Codex; ask the user for variable names, not values.
- Authority is `https://login.microsoftonline.com/consumers`: the app registrations target personal Microsoft accounts.
- `app/entry.client.tsx` is a custom client entry. It creates and `initialize()`s the `PublicClientApplication` and wraps `<HydratedRouter/>` in `<MsalProvider>`. Keep MSAL here, out of `root.tsx`/route modules: it is the browser-only boundary.
- `loginRequest` asks for the API scope itself, not Graph, so consent and the API token come from login. Otherwise `acquireTokenSilent` falls back to a hidden iframe, which times out for personal accounts. `prompt: "select_account"` is set so users can switch accounts.
- Route guards live in the route modules: `routes/login.tsx` redirects to `/reservas` when authenticated, and `routes/reservas.tsx` redirects to `/login` when not. Both wait for `inProgress === InteractionStatus.None` before deciding.
- Logout is local only: `instance.clearCache()`, followed by a hard `window.location.assign("/login")`. `clearCache()` emits no MSAL event, so `MsalProvider` state would otherwise stay stale. `logoutRedirect()` would also sign the user out of Microsoft everywhere.
- `components/reservas/reservas-panel.tsx` gets the API token (`acquireTokenSilent`, with redirect fallback on `InteractionRequiredAuthError` or `timed_out`) and calls `http://localhost:8080/api/home`. The backend URL is hardcoded.

## Backend architecture

- Spring Boot **4.0.8**. Do not bump to 4.1.x without checking Spring Cloud Azure support. `spring-cloud-azure-starter-active-directory` has no version in `pom.xml`: it comes from the imported `spring-cloud-azure-dependencies` BOM (7.4.0). Don't pin the starter directly.
- Resource server only: `config/SecurityConfig.java` requires a JWT on every request, plus a CORS bean allowing `http://localhost:5173`. The Azure starter auto-configures JWT validation (issuer, audience) from `application.yaml` (`spring.cloud.azure.active-directory.*`).
- `profile.tenant-id` must be the consumers tenant GUID `9188040d-6c67-4c5b-b112-36a304b66dad`. The starter rejects the `consumers` alias for resource servers. Tokens from the org tenant `569ca4b8-...` (such as client-credentials tokens) are correctly rejected with 401.
- JPA entities in `model/` (`Usuario`, `Gimnasio`, `Horario`, `Reserva`, `EstadoReserva`) use Lombok and an in-memory H2 database. No repositories or services exist yet. `controller/TestAuthController.java` (`GET /api/home`) is a placeholder for testing auth.

## Azure app registrations

- **ReservaFit-Frontend**: client ID `8b56e001-8f57-4bd1-9329-ffdea5aeeae6`, SPA redirect URI `http://localhost:5173`.
- **ReservaFit-API**: client ID `f4a5103b-ae57-406d-85e1-eee64edbc4ac`, exposes `api://f4a5103b-ae57-406d-85e1-eee64edbc4ac/access_as_user`.

To test the API from Postman, either copy the `Bearer` token from the browser's Network tab, or add a "Mobile and desktop" redirect URI (`https://oauth.pstmn.io/v1/callback`) to the frontend registration. Postman can't redeem codes for the SPA redirect URI (`AADSTS90023`).
