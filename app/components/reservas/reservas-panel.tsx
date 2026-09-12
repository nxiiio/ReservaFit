import { InteractionStatus } from "@azure/msal-browser";
import { useMsal } from "@azure/msal-react";

export function ReservasPanel() {
  const { instance, accounts, inProgress } = useMsal();
  const account = accounts[0];
  const isBusy = inProgress !== InteractionStatus.None;

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

  return (
    <section className="mx-auto w-full max-w-2xl px-5 py-12 sm:px-8">
      <p className="mb-2 text-xs font-bold tracking-[0.16em] text-muted uppercase">Sesión iniciada</p>
      <h1 className="font-display text-4xl font-bold uppercase">
        Ya estás verificado{account?.name ? `, ${account.name}` : ""}
      </h1>
      <p className="mt-4 text-sm leading-6 text-muted">
        Esta pantalla es un placeholder: todavía no hay funcionalidad de reservas, solo confirma que el login con Microsoft funciona de punta a punta.
      </p>

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
