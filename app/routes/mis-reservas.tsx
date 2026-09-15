import type { Route } from "./+types/mis-reservas";
import { MyBookingsPanel } from "../components/reservas/my-bookings-panel";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Mis reservas | ReservaFit" },
    { name: "description", content: "Tus reservas en ReservaFit." },
  ];
}

export default function MisReservas() {
  return <MyBookingsPanel />;
}
