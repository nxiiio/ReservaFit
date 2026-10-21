import { InteractionStatus } from "@azure/msal-browser";
import { useIsAuthenticated, useMsal } from "@azure/msal-react";
import { Navigate, useNavigate } from "react-router";
import type { Route } from "./+types/login";
import { LoginHeader } from "../components/login/login-header";
import { LoginPanel } from "../components/login/login-panel";
import { LoginVisual } from "../components/login/login-visual";
import { CompleteProfileForm } from "../components/login/complete-profile-form";
import { AlertIcon } from "../components/ui/icons";
import { useCurrentUser } from "../lib/use-current-user";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Iniciar sesión | ReservaFit" },
    { name: "description", content: "Prueba el inicio de sesión de ReservaFit. Tu próximo entrenamiento empieza cerca." },
  ];
}

export default function Login() {
  const isAuthenticated = useIsAuthenticated();
  const { inProgress } = useMsal();
  const navigate = useNavigate();
  const signedIn = isAuthenticated && inProgress === InteractionStatus.None;
  const { state, retry } = useCurrentUser(signedIn);

  if (signedIn && state.status === "ready" && state.profile.profileComplete) {
    return <Navigate to="/reservas" replace />;
  }

  let content;
  if (!signedIn) {
    content = <LoginPanel />;
  } else if (state.status === "ready") {
    content = (
      <CompleteProfileForm profile={state.profile} onCompleted={() => navigate("/reservas", { replace: true })} />
    );
  } else if (state.status === "error") {
    content = (
      <div className="enter mx-auto w-full max-w-md py-5 text-sm leading-6" role="alert">
        <AlertIcon className="mb-4 size-6 text-accent-deep" />
        <p className="font-display text-3xl leading-none font-bold uppercase">No pudimos verificar tu cuenta.</p>
        <p className="mt-3 text-base text-muted">Revisa tu conexión e intenta de nuevo.</p>
        <button
          type="button"
          onClick={retry}
          className="press mt-6 flex h-13 cursor-pointer items-center justify-center rounded-lg border border-line-strong bg-white px-6 font-semibold text-ink hover:border-ink"
        >
          Reintentar
        </button>
      </div>
    );
  } else {
    content = (
      <p className="enter mx-auto flex w-full max-w-md items-center gap-3 py-5 text-sm text-muted" role="status">
        <span aria-hidden="true" className="skeleton h-2 w-16 rounded-full" />
        Verificando tu cuenta…
      </p>
    );
  }

  return (
    <>
      <LoginHeader />
      <main className="mx-auto grid w-full max-w-7xl flex-1 gap-10 px-5 pb-8 sm:px-8 lg:grid-cols-[1fr_1.05fr] lg:items-stretch lg:gap-16 lg:px-12 lg:pb-10">
        <div className="flex items-center">{content}</div>
        <LoginVisual />
      </main>
    </>
  );
}
