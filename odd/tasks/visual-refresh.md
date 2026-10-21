# Visual refresh (frontend)

## Objective
Refine the existing ReservaFit UI identity across every page: hierarchy, spacing, states, micro-interactions and entrance motion. Visual and motion only.

## Scope and constraints
- Refinement, not redesign (user choice, 2026-10-20): keep paper `#faf9f6`, ink `#252721`, accent `#ff6b35`, Barlow Condensed + DM Sans, Spanish copy, routes, behavior.
- No new features, no API or logic changes, no new runtime dependencies (motion via CSS in `app/app.css`).
- Every animation honors `prefers-reduced-motion`.
- Branch: `design/visual-refresh` (from `rabbitmq`).

## Tasks
- [x] T1 Tokens and motion primitives in `app/app.css` (route: delegated writer, 2+ non-trivial files)
- [x] T2 Landing (`/`) and login (`/login`, complete-profile form)
- [x] T3 Signed-in pages: nav, gym catalog, gym booking, my bookings
- [x] T4 Checks: `pnpm typecheck`, `pnpm build`, impeccable detector, browser pass (landing and login at 1280px and 375px; signed-in pages not viewed, they need a Microsoft sign-in)
- [x] T5 Port the refresh onto the notifications branch and style the notification bell

## Checks
No frontend test runner exists, so no RED/GREEN; checks are typecheck, build, detector and a visual pass.

## Progress
- Branch created. Engram mirror pending (Engram unavailable this session).
- T1 to T3 done by one delegated writer (route: delegated, 2+ non-trivial files). Uncommitted; the parent commits.
  - Tokens: `accent-deep`, `accent-hover`, `surface`, `line-strong`, ink-tinted `shadow-card`/`shadow-lift`/`shadow-pop`, `ease-out-expo`; radius rule documented at the top of `app/app.css`.
  - Motion primitives in `app/app.css` (`rise`, `enter`, `enter-x`, `line-mask`, `settle`, `pop`, `reveal*`, `lift`, `press`, `nudge`, `skeleton`, `nav-bar`), all gated by `prefers-reduced-motion: no-preference`; scroll reveal via `app/hooks/use-reveal.ts` (IntersectionObserver, content visible without JS).
  - Shared UI: `app/components/ui/icons.tsx`, `app/components/ui/state-block.tsx`.
- Checks: `pnpm typecheck` exit 0; `pnpm build` OK (SPA prerender generated); `impeccable detect --json app` returned `[]`.
- Known kept items (copy is frozen): hero tagline under the CTAs and the two eyebrows ("Cómo funciona", "Tu próximo paso empieza aquí") stay because removing them is a copy change.
- Next step: browser pass (desktop and mobile, reduced motion on and off), then commit.
- T5 (route: delegated writer): the refresh was stash-popped onto `notifications` (18c0e95). The only conflict, `app/components/reservas/app-nav.tsx`, was resolved by keeping the bell plus button cluster from 18c0e95 and the refreshed user-menu styling (`press`, `bg-surface`, `pop`, `shadow-pop`); marked resolved with `git add`, not committed, stash left in place. `gym-booking-panel.tsx` and `my-bookings-panel.tsx` auto-merged with their `requestNotificationsRefresh()` calls intact.
  - Bell restyled (visual only, logic, polling, aria and copy unchanged): tactile round button matching the avatar, unread badge in ink on accent (5.5:1) with a paper ring, `pop` panel with `shadow-pop` and `rounded-lg`, unread rows on `bg-surface/60` with semibold text and an `accent-deep` dot, drawn `CheckIcon`/`CloseIcon` replacing the glyphs, empty state through `StateBlock`. New `BellIcon` and `CloseIcon` in `app/components/ui/icons.tsx`; radius rule now names both round nav buttons.
  - Mobile: the bell wrapper is only `relative` from `sm`, so on phones the 20rem panel anchors to the nav button cluster (right gutter) and stays on screen at 375px.
  - No loading or error UI added: the bell has no such state or copy (errors are logged and retried by the next poll).
  - Checks: no conflicted files, no conflict markers in `app`, `pnpm typecheck` clean, `pnpm build` OK (SPA prerender generated), `impeccable detect --json app/components/reservas` returned `[]`. Not viewed in a browser (signed-in only).
