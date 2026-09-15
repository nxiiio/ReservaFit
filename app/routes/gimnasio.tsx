import type { Route } from "./+types/gimnasio";
import { GymBookingPanel } from "../components/reservas/gym-booking-panel";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Reservar | ReservaFit" },
    { name: "description", content: "Elige un día y un horario para reservar en tu gimnasio de barrio." },
  ];
}

export default function Gimnasio({ params }: Route.ComponentProps) {
  // key: reset all page state when navigating between gyms
  return <GymBookingPanel key={params.id} gymId={params.id} />;
}
