import { InteractionStatus } from "@azure/msal-browser";
import { useIsAuthenticated, useMsal } from "@azure/msal-react";
import { Navigate } from "react-router";
import type { Route } from "./+types/reservas";
import { ReservasPanel } from "../components/reservas/reservas-panel";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Mis reservas | ReservaFit" },
    { name: "description", content: "Panel de reservas de ReservaFit." },
  ];
}

export default function Reservas() {
  const isAuthenticated = useIsAuthenticated();
  const { inProgress } = useMsal();

  if (!isAuthenticated && inProgress === InteractionStatus.None) {
    return <Navigate to="/login" replace />;
  }

  return <ReservasPanel />;
}
