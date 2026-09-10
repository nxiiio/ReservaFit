# OAuth2 login via MSAL + Microsoft Entra ID

Date: 2026-09-10

## Context

ReservaFit currently has a custom username/password login and register flow
implemented client-side only (no backend). The course requires replacing
this with OAuth2 authentication against Microsoft Entra ID using MSAL, with
a Spring Boot backend (`ReservaFit-Backend/`, currently an untracked,
unconfigured skeleton in this same working directory) validating the
resulting tokens.

Azure App Registrations already created (student subscription, single
tenant):

- Tenant ID: `569ca4b8-5e33-460a-8759-2bc12cb2b1e0`
- Frontend SPA Client ID: `f0157564-c97e-4410-82f4-e4728b087339`
  (redirect URI: `http://localhost:5173`)
- Backend API Client ID: `6c072ff1-3e73-4aa2-9a95-4c4f459e99c9`
  (exposes scope `access_as_user`)

## Goal

End-to-end OAuth2 login: the React SPA authenticates the user against Entra
ID via MSAL.js, acquires an access token scoped to the backend API, and the
Spring Boot backend validates that token on at least one protected endpoint.

## Non-goals (explicitly out of scope for this change)

- No local username/password login or self-service registration — Entra ID
  owns identity entirely.
- No persistence of the authenticated user in the backend database (H2/JPA).
  No `User` entity is introduced. The only backend behavior is JWT
  validation and returning the token's claims.
- No design for the eventual "reservations" feature. The post-login screen
  is a placeholder that proves the auth flow works, not a real feature.
- No automated tests that exercise a real Entra ID login (would require
  mocking the IdP). Verification is manual, described below.

## Architecture

```
React SPA (MSAL.js, public client)
   │  loginPopup() → Entra ID authorization code + PKCE flow
   │  acquires access token, audience = backend API scope
   ▼
Spring Boot (OAuth2 resource server, NOT an OAuth2 client)
   │  validates JWT: issuer = tenant, audience = backend API client ID
   ▼
GET /api/me → returns claims from the validated token
```

Two Entra ID App Registrations are required and already exist (see IDs
above): one for the SPA (public client, no secret) and one for the API
(exposes the `access_as_user` scope, used only for audience validation —
it does not need its own credentials since the backend never authenticates
itself to Entra ID, it only verifies tokens issued for it).

## Frontend design

- Add `@azure/msal-browser` and `@azure/msal-react` to `package.json`.
- New `app/lib/msal-config.ts`:
  ```ts
  export const msalConfig = {
    auth: {
      clientId: "f0157564-c97e-4410-82f4-e4728b087339",
      authority: "https://login.microsoftonline.com/569ca4b8-5e33-460a-8759-2bc12cb2b1e0",
      redirectUri: "http://localhost:5173",
    },
  };
  export const apiScope = "api://6c072ff1-3e73-4aa2-9a95-4c4f459e99c9/access_as_user";
  ```
- `app/routes/layout.tsx` wraps its children in `MsalProvider` (instantiate
  `PublicClientApplication` from the config above).
- **Login flow uses `loginPopup`, not `loginRedirect`.** React Router runs
  SSR; `loginRedirect` requires handling `handleRedirectPromise()` in a
  client-only effect on the page the browser lands back on, which adds
  hydration-ordering complexity. `loginPopup` is a single call triggered
  directly by the button's click handler — no extra route, no SSR
  interaction — while using the same Authorization Code + PKCE flow
  underneath. This is the right tradeoff for this project's scope.
- `app/routes/login.tsx` / `app/components/login/login-form.tsx`: replace
  the existing form entirely with a single "Iniciar sesión con Microsoft"
  button. On click: `instance.loginPopup({ scopes: [apiScope] })`; on
  success, navigate to `/reservas`. On failure or popup dismissal, show an
  inline error message — do not throw.
- Delete `app/routes/register.tsx` and `app/components/register/` entirely.
  Remove the `register` route from `app/routes.ts`.
- New `app/routes/reservas.tsx` (+ a small component), added to
  `app/routes.ts`:
  - If `useIsAuthenticated()` is false, redirect to `/login`.
  - On mount, get a token via `acquireTokenSilent({ scopes: [apiScope] })`,
    falling back to `acquireTokenPopup` if silent acquisition fails (e.g.
    expired session).
  - Call `GET http://localhost:8080/api/me` with
    `Authorization: Bearer <token>`.
  - Render the returned claims (name, email) as placeholder content — this
    page exists to prove the flow works end-to-end, not as a real feature.

## Backend design

- `pom.xml`: replace `spring-boot-starter-security-oauth2-client` and
  `spring-boot-starter-security-oauth2-client-test` with
  `spring-boot-starter-oauth2-resource-server` and its `-test` counterpart.
  The `-client` starter is for an app that performs its own OAuth2/OIDC
  login (authorization code flow, session, confidential client); this
  backend never logs in anywhere, it only validates bearer tokens it
  receives, which is the resource-server starter's job.
- `src/main/resources/application.yaml`:
  ```yaml
  spring:
    security:
      oauth2:
        resourceserver:
          jwt:
            issuer-uri: https://login.microsoftonline.com/569ca4b8-5e33-460a-8759-2bc12cb2b1e0/v2.0
  ```
- New `SecurityConfig` (`@Configuration`, `SecurityFilterChain` bean):
  - Require authentication on `/api/**`.
  - Enable `.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))`.
  - **Audience validation.** Spring's default JWT decoder (built from
    `issuer-uri`) validates the issuer and signature but does not by
    default reject a token whose `aud` claim isn't this API. Any valid
    Entra ID token from the same tenant — even one issued for an unrelated
    app — would otherwise pass. Add a custom validator (combine
    `JwtIssuerValidator` with an audience check against
    `6c072ff1-3e73-4aa2-9a95-4c4f459e99c9`) and set it via
    `NimbusJwtDecoder#setJwtValidator`.
  - CORS: allow origin `http://localhost:5173` for `/api/**`.
- New `MeController`: `GET /api/me`, takes `@AuthenticationPrincipal Jwt
  jwt`, returns a JSON body built from `jwt.getClaim("name")`,
  `jwt.getClaim("preferred_username")`, `jwt.getClaim("oid")`. No
  persistence — this is a read of the token's claims only.

## Repo integration

- `ReservaFit-Backend/` becomes part of this repo (monorepo), confirmed
  clean of its own `.git`. Merge its `.gitignore` (Maven `target/`, IDE
  dirs) into the root `.gitignore` rather than keeping a nested one.

## Error handling

- Frontend: popup failure/dismissal → inline error, no crash. Silent token
  acquisition failure → fallback to popup.
- Backend: invalid/expired/wrong-audience token → Spring's resource server
  filter returns `401` automatically; no custom handling needed.

## Testing / verification

No automated test covers a real Entra ID login (would require mocking the
IdP, out of scope). Manual verification:

1. Run `pnpm dev` (port 5173) and `./mvnw spring-boot:run` (port 8080)
   together.
2. Log in through the real Microsoft popup from `/login`.
3. Confirm `/reservas` renders the claims returned by `/api/me`.
4. Confirm calling `/api/me` without a token, or with a token for a
   different audience, returns `401`.

## Files touched

**Frontend**: `app/lib/msal-config.ts` (new), `app/routes/layout.tsx`,
`app/routes/login.tsx`, `app/components/login/login-form.tsx`,
`app/routes/reservas.tsx` (new) + component, `app/routes.ts`,
`package.json`. Deleted: `app/routes/register.tsx`,
`app/components/register/`.

**Backend**: `ReservaFit-Backend/pom.xml`,
`ReservaFit-Backend/src/main/resources/application.yaml`, new
`SecurityConfig.java`, new `MeController.java`.

**Root**: `.gitignore` (merge in Java/Maven ignore rules).
