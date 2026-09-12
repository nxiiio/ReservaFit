import { InteractionStatus } from "@azure/msal-browser";
import { useMsal } from "@azure/msal-react";
import { loginRequest } from "../../authConfig";

export function LoginPanel() {
  const { instance, inProgress } = useMsal();
  const isBusy = inProgress !== InteractionStatus.None;

  function handleLogin() {
    instance.loginRedirect(loginRequest).catch(console.error);
  }

  return (
    <section aria-labelledby="login-title" className="mx-auto w-full max-w-md py-5 sm:py-10 lg:py-12">
      <p className="mb-5 flex items-center gap-2 text-xs font-bold tracking-[0.16em] text-muted uppercase">
        <span aria-hidden="true" className="size-2 rounded-full bg-accent" /> Tu próximo entrenamiento
      </p>
      <h1 id="login-title" className="font-display text-6xl leading-none font-bold tracking-tight uppercase sm:text-7xl">
        Qué bueno<br />verte <span className="text-[#b94018]">de nuevo.</span>
      </h1>
      <p className="mt-5 text-base leading-7 text-muted">Tu barrio, tu gimnasio y un momento para ti.</p>

      <button
        type="button"
        onClick={handleLogin}
        disabled={isBusy}
        className="mt-8 flex h-13 w-full items-center justify-center gap-3 rounded-lg border border-line bg-white text-sm font-semibold text-ink transition-colors hover:bg-paper disabled:cursor-not-allowed disabled:opacity-50"
      >
        <img src="/icons/microsoft-logo.svg" alt="" aria-hidden="true" className="size-5" />
        Iniciar sesión con Microsoft
      </button>
    </section>
  );
}
