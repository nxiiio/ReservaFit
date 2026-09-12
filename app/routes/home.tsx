import type { Route } from "./+types/home";
import { LandingNav } from "../components/landing/landing-nav";
import { Hero } from "../components/landing/hero";
import { Features } from "../components/landing/features";
import { HowItWorks } from "../components/landing/how-it-works";
import { FinalCta } from "../components/landing/final-cta";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "ReservaFit | Tu próximo entrenamiento empieza cerca" },
    {
      name: "description",
      content:
        "Descubre gimnasios independientes en tu comuna, elige tu horario y reserva tu próximo entrenamiento con ReservaFit.",
    },
  ];
}

export default function Home() {
  return (
    <>
      <a href="#contenido" className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4 focus:z-50 focus:rounded-lg focus:bg-ink focus:px-5 focus:py-3 focus:text-white">
        Saltar al contenido
      </a>
      <LandingNav />
      <main id="contenido">
        <Hero />
        <Features />
        <HowItWorks />
        <FinalCta />
      </main>
    </>
  );
}
