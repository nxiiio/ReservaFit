import type { Route } from "./+types/reservas";
import { ReservasPanel } from "../components/reservas/reservas-panel";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Mis reservas | ReservaFit" },
    { name: "description", content: "Panel de reservas de ReservaFit." },
  ];
}

export default function Reservas() {
  return <ReservasPanel />;
}
