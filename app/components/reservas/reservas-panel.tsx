import { useEffect, useState } from "react";
import { Navigate } from "react-router";
import { InteractionStatus } from "@azure/msal-browser";
import { useIsAuthenticated, useMsal } from "@azure/msal-react";
import { apiScope } from "../../lib/msal-config";

type ApiMe = { name?: string; preferred_username?: string; oid?: string };

export function ReservasPanel() {
  const isAuthenticated = useIsAuthenticated();
  const { instance, accounts, inProgress } = useMsal();
  const [apiResult, setApiResult] = useState<ApiMe | null>(null);
  const [apiError, setApiError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated || accounts.length === 0) return;

    let cancelled = false;

    async function callApi() {
      const account = accounts[0];
      try {
        let accessToken: string;
        try {
          const result = await instance.acquireTokenSilent({ scopes: [apiScope], account });
          accessToken = result.accessToken;
        } catch (err) {
          console.error(err);
          const result = await instance.acquireTokenPopup({ scopes: [apiScope], account });
          accessToken = result.accessToken;
        }

        const response = await fetch("http://localhost:8080/api/me", {
          headers: { Authorization: `Bearer ${accessToken}` },
        });
        if (!response.ok) throw new Error(`status ${response.status}`);
        const data = (await response.json()) as ApiMe;
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
  }, [isAuthenticated, accounts, instance]);

  if (inProgress === InteractionStatus.Startup) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  const account = accounts[0];

  return (
    <section className="mx-auto w-full max-w-2xl px-5 py-12 sm:px-8">
      <p className="mb-2 text-xs font-bold tracking-[0.16em] text-muted uppercase">Sesión iniciada</p>
      <h1 className="font-display text-4xl font-bold uppercase">Hola, {account?.name ?? account?.username}</h1>
      <p className="mt-4 text-sm leading-6 text-muted">
        Esta pantalla es un placeholder: todavía no hay funcionalidad de reservas, solo confirma que el login con Microsoft funciona de punta a punta.
      </p>

      <div className="mt-8 rounded-lg border border-line bg-white p-5 text-sm leading-6">
        <p className="font-semibold">Respuesta del backend (/api/me)</p>
        {apiResult && <pre className="mt-2 overflow-x-auto text-xs">{JSON.stringify(apiResult, null, 2)}</pre>}
        {apiError && <p className="mt-2 text-[#b94018]">{apiError}</p>}
        {!apiResult && !apiError && <p className="mt-2 text-muted">Conectando…</p>}
      </div>
    </section>
  );
}
