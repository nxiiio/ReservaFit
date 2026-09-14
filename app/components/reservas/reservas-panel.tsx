import { InteractionStatus } from "@azure/msal-browser";
import { useMsal } from "@azure/msal-react";
import { Navigate } from "react-router";
import { useCurrentUser } from "../../lib/use-current-user";

export function ReservasPanel() {
  const { instance, accounts, inProgress } = useMsal();
  const account = accounts[0];
  const isBusy = inProgress !== InteractionStatus.None;
  const { state, retry } = useCurrentUser(Boolean(account));

  function handleLogout() {
    // clearCache() only wipes the local MSAL cache — unlike logoutRedirect(),
    // it never navigates to Microsoft's logout endpoint, so it doesn't end
    // the browser's Microsoft SSO session (Outlook, Teams, etc. stay logged in).
    // It also doesn't fire an MSAL event, so MsalProvider's React state (and
    // therefore the /reservas guard) would never notice the account is gone —
    // a hard navigation forces a fresh read of the (now empty) cache on load.
    instance
      .clearCache({ account })
      .then(() => window.location.assign("/login"))
      .catch(console.error);
  }

  if (state.status === "ready" && !state.profile.profileComplete) {
    return <Navigate to="/login" replace />;
  }

  const profile = state.status === "ready" ? state.profile : null;

  return (
    <section className="mx-auto w-full max-w-2xl px-5 py-12 sm:px-8">
      <p className="mb-2 text-xs font-bold tracking-[0.16em] text-muted uppercase">Sesión iniciada</p>
      <h1 className="font-display text-4xl font-bold uppercase">
        Ya estás verificado{profile?.name ? `, ${profile.name}` : ""}
      </h1>
      <p className="mt-4 text-sm leading-6 text-muted">
        Esta pantalla es un placeholder: todavía no hay funcionalidad de reservas, solo confirma que tu cuenta está registrada en ReservaFit.
      </p>

      <div className="mt-8 rounded-lg border border-line bg-white p-5 text-sm leading-6">
        <p className="font-semibold">Tu perfil en el backend</p>
        {profile && (
          <dl className="mt-2 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1">
            <dt className="text-muted">Nombre</dt>
            <dd>{profile.name ?? "—"}</dd>
            <dt className="text-muted">Email</dt>
            <dd>{profile.email ?? "—"}</dd>
            <dt className="text-muted">Registrado</dt>
            <dd>{profile.registeredAt ? new Date(profile.registeredAt).toLocaleString("es-CL") : "—"}</dd>
          </dl>
        )}
        {state.status === "error" && (
          <p className="mt-2 text-[#b94018]">
            No se pudo conectar con el backend.{" "}
            <button type="button" onClick={retry} className="cursor-pointer font-semibold underline">
              Reintentar
            </button>
          </p>
        )}
        {state.status === "loading" && <p className="mt-2 text-muted">Conectando…</p>}
      </div>

      <button
        type="button"
        onClick={handleLogout}
        disabled={isBusy}
        className="mt-8 flex h-13 items-center justify-center rounded-lg border border-line bg-white px-5 text-sm font-semibold text-ink transition-colors hover:bg-paper disabled:cursor-not-allowed disabled:opacity-50"
      >
        Cerrar sesión
      </button>
    </section>
  );
}
