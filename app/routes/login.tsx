import { InteractionStatus } from "@azure/msal-browser";
import { useIsAuthenticated, useMsal } from "@azure/msal-react";
import { Navigate } from "react-router";
import type { Route } from "./+types/login";
import { LoginHeader } from "../components/login/login-header";
import { LoginPanel } from "../components/login/login-panel";
import { LoginVisual } from "../components/login/login-visual";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Iniciar sesión | ReservaFit" },
    { name: "description", content: "Prueba el inicio de sesión de ReservaFit. Tu próximo entrenamiento empieza cerca." },
  ];
}

export default function Login() {
  const isAuthenticated = useIsAuthenticated();
  const { inProgress } = useMsal();

  if (isAuthenticated && inProgress === InteractionStatus.None) {
    return <Navigate to="/reservas" replace />;
  }

  return (
    <>
      <LoginHeader />
      <main className="mx-auto grid w-full max-w-7xl flex-1 gap-10 px-5 pb-8 sm:px-8 lg:grid-cols-[1fr_1.05fr] lg:items-stretch lg:gap-16 lg:px-12 lg:pb-10">
        <div className="flex items-center"><LoginPanel /></div>
        <LoginVisual />
      </main>
    </>
  );
}
