import { useEffect, useState } from "react";
import {
  BrowserAuthError,
  BrowserAuthErrorCodes,
  InteractionRequiredAuthError,
  InteractionStatus,
} from "@azure/msal-browser";
import { useMsal } from "@azure/msal-react";
import { apiRequest } from "../../authConfig";

type ApiHome = { timestamp: string; message: string };

export function ReservasPanel() {
  const { instance, accounts, inProgress } = useMsal();
  const account = accounts[0];
  const isBusy = inProgress !== InteractionStatus.None;
  const [apiResult, setApiResult] = useState<ApiHome | null>(null);
  const [apiError, setApiError] = useState<string | null>(null);

  useEffect(() => {
    if (!account) return;

    let cancelled = false;

    async function callApi() {
      try {
        let accessToken: string;
        try {
          const result = await instance.acquireTokenSilent({ ...apiRequest, account });
          accessToken = result.accessToken;
        } catch (err) {
          const needsInteraction =
            err instanceof InteractionRequiredAuthError ||
            (err instanceof BrowserAuthError && err.errorCode === BrowserAuthErrorCodes.timedOut);
          if (needsInteraction) {
            await instance.acquireTokenRedirect(apiRequest);
            return;
          }
          throw err;
        }

        const response = await fetch("http://localhost:8080/api/home", {
          headers: { Authorization: `Bearer ${accessToken}` },
        });
        if (!response.ok) throw new Error(`status ${response.status}`);
        const data = (await response.json()) as ApiHome;
        if (!cancelled) setApiResult(data);
      } catch (err) {
        console.error(err);
        if (!cancelled) setApiError("No se pudo conectar con el backend todavía.");
      }
    }

    callApi();
    return () => {
      cancelled = true;
    };
  }, [account, instance]);

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

      <div className="mt-8 rounded-lg border border-line bg-white p-5 text-sm leading-6">
        <p className="font-semibold">Respuesta del backend (/api/home)</p>
        {apiResult && <pre className="mt-2 overflow-x-auto text-xs">{JSON.stringify(apiResult, null, 2)}</pre>}
        {apiError && <p className="mt-2 text-[#b94018]">{apiError}</p>}
        {!apiResult && !apiError && <p className="mt-2 text-muted">Conectando…</p>}
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
