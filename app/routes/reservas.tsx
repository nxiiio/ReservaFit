import type { Route } from "./+types/reservas";
import { ReservasPanel } from "../components/reservas/reservas-panel";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Gimnasios | ReservaFit" },
    { name: "description", content: "Gimnasios de barrio disponibles en ReservaFit." },
  ];
}

// Auth and profile guards live in routes/app-layout.tsx
export default function Reservas() {
  return <ReservasPanel />;
}
