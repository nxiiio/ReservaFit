import type { CSSProperties } from "react";
import { Link } from "react-router";
import { ArrowIcon } from "./arrow-icon";

const lines = [
  { text: "Tu barrio.", accent: false },
  { text: "Tu gimnasio.", accent: false },
  { text: "Tu momento.", accent: true },
];

function delay(index: number) {
  return { "--i": index } as CSSProperties;
}

export function Hero() {
  return (
    <section aria-labelledby="hero-title" className="mx-auto grid max-w-7xl gap-10 px-5 pt-10 pb-16 sm:px-8 sm:pt-14 lg:grid-cols-[1.05fr_1fr] lg:items-center lg:gap-14 lg:px-12 lg:pt-16 lg:pb-20">
      <div>
        <h1 id="hero-title" className="font-display text-[clamp(4.2rem,7.4vw,6rem)] leading-[0.9] font-bold tracking-[-0.02em] uppercase">
          {lines.map((line, index) => (
            <span key={line.text} className="line-mask block">
              <span className={`block ${line.accent ? "text-accent-deep" : ""}`} style={delay(index)}>
                {line.text}
              </span>
            </span>
          ))}
        </h1>
        <p className="rise mt-8 max-w-md text-base leading-7 text-muted sm:text-lg" style={delay(3)}>
          Encuentra tu lugar para entrenar, elige un horario y reserva. Así de simple. Así de cerca.
        </p>
        <div className="rise mt-9 flex flex-wrap items-center gap-x-7 gap-y-5" style={delay(4)}>
          <Link to="/login" className="press group inline-flex items-center gap-6 rounded-lg bg-accent px-6 py-4 text-sm font-bold text-ink hover:bg-accent-hover">
            Empezar ahora <ArrowIcon className="nudge size-4" />
          </Link>
          <a href="#como-funciona" className="group inline-flex items-center gap-2 py-3 text-sm font-semibold underline decoration-line-strong decoration-1 underline-offset-[6px] transition-colors hover:decoration-ink">
            Cómo funciona <ArrowIcon className="nudge size-3.5 rotate-45" />
          </a>
        </div>
        <p className="rise mt-8 text-xs text-muted" style={delay(5)}>Elige tu momento para ir a entrenar.</p>
      </div>
      <div className="relative overflow-hidden rounded-2xl bg-ink">
        <img
          src="https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=1200&q=85"
          alt="Zona de entrenamiento con pesas y equipamiento de gimnasio"
          width={1200}
          height={1400}
          fetchPriority="high"
          className="settle aspect-[6/5] w-full object-cover brightness-75 lg:aspect-[6/7]"
        />
        <div className="absolute inset-0 bg-linear-to-t from-black/75 via-transparent to-transparent" aria-hidden="true" />
        <div className="rise absolute right-6 bottom-6 left-6 flex items-end justify-between gap-4 text-white sm:right-8 sm:bottom-8 sm:left-8" style={delay(6)}>
          <p className="max-w-56 font-display text-4xl leading-none font-semibold uppercase sm:text-5xl">Un buen lugar.<br />Un nuevo comienzo.</p>
          <span aria-hidden="true" className="flex size-12 shrink-0 items-center justify-center rounded-full border border-white/60"><ArrowIcon className="size-6 -rotate-45" variant="white" /></span>
        </div>
      </div>
    </section>
  );
}
