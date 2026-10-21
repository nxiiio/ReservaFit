import { InteractionStatus } from "@azure/msal-browser";
import { useMsal } from "@azure/msal-react";
import type { CSSProperties } from "react";
import { loginRequest } from "../../authConfig";

export function LoginPanel() {
  const { instance, inProgress } = useMsal();
  const isBusy = inProgress !== InteractionStatus.None;

  function handleLogin() {
    instance.loginRedirect(loginRequest).catch(console.error);
  }

  return (
    <section aria-labelledby="login-title" className="mx-auto w-full max-w-md py-5 sm:py-10 lg:py-12">
      <p className="rise mb-5 text-xs font-bold tracking-[0.16em] text-muted uppercase">Tu próximo entrenamiento</p>
      <h1 id="login-title" className="rise font-display text-6xl leading-[0.92] font-bold tracking-[-0.02em] uppercase sm:text-7xl" style={{ "--i": 1 } as CSSProperties}>
        Qué bueno<br />verte <span className="text-accent-deep">de nuevo.</span>
      </h1>
      <p className="rise mt-6 text-base leading-7 text-muted" style={{ "--i": 2 } as CSSProperties}>Tu barrio, tu gimnasio y un momento para ti.</p>

      <button
        type="button"
        onClick={handleLogin}
        disabled={isBusy}
        style={{ "--i": 3 } as CSSProperties}
        className="press rise mt-9 flex h-13 w-full cursor-pointer items-center justify-center gap-3 rounded-lg border border-line-strong bg-white text-sm font-semibold text-ink shadow-card hover:border-ink disabled:cursor-not-allowed disabled:opacity-50"
      >
        <img src="/icons/microsoft-logo.svg" alt="" aria-hidden="true" className="size-5" />
        Iniciar sesión con Microsoft
      </button>
    </section>
  );
}
