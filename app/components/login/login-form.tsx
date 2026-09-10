import { useState } from "react";
import { useNavigate } from "react-router";
import { useMsal } from "@azure/msal-react";
import { apiScope } from "../../lib/msal-config";
import { ArrowIcon } from "../landing/arrow-icon";

export function LoginForm() {
  const { instance } = useMsal();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleLogin() {
    setError(null);
    setLoading(true);
    try {
      await instance.loginPopup({ scopes: [apiScope] });
      navigate("/reservas");
    } catch {
      setError("No se pudo iniciar sesión. Intenta de nuevo.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section aria-labelledby="login-title" className="mx-auto w-full max-w-md py-5 sm:py-10 lg:py-12">
      <p className="mb-5 flex items-center gap-2 text-xs font-bold tracking-[0.16em] text-muted uppercase">
        <span aria-hidden="true" className="size-2 rounded-full bg-accent" /> Tu próximo entrenamiento
      </p>
      <h1 id="login-title" className="font-display text-6xl leading-none font-bold tracking-tight uppercase sm:text-7xl">
        Qué bueno<br />verte <span className="text-[#b94018]">de nuevo.</span>
      </h1>
      <p className="mt-5 text-base leading-7 text-muted">Inicia sesión con tu cuenta de Microsoft para reservar tu próximo entrenamiento.</p>

      <button type="button" onClick={handleLogin} disabled={loading} className="mt-7 flex min-h-13 w-full cursor-pointer items-center justify-between gap-4 rounded-lg bg-accent px-5 py-4 text-sm font-bold transition-colors hover:bg-[#f15d28] disabled:cursor-not-allowed disabled:opacity-60">
        {loading ? "Conectando…" : "Iniciar sesión con Microsoft"} <ArrowIcon className="size-5" />
      </button>
      <div role="status" aria-live="polite" aria-atomic="true">
        {error && (
          <p className="mt-5 rounded-lg border border-line bg-white px-4 py-3 text-sm leading-6 text-[#b94018]">{error}</p>
        )}
      </div>
    </section>
  );
}
