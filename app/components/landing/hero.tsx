import { Link } from "react-router";
import { ArrowIcon } from "./arrow-icon";

export function Hero() {
  return (
    <section aria-labelledby="hero-title" className="mx-auto grid max-w-7xl gap-10 px-5 pt-10 pb-14 sm:px-8 sm:pt-14 lg:grid-cols-[1.05fr_1fr] lg:items-center lg:gap-12 lg:px-12 lg:py-16">
      <div>
        <h1 id="hero-title" className="font-display text-[clamp(4.2rem,7.4vw,6.8rem)] leading-[0.9] font-bold tracking-tight uppercase">
          Tu barrio.<br />Tu gimnasio.<br /><span className="text-[#b94018]">Tu momento.</span>
        </h1>
        <p className="mt-7 max-w-md text-base leading-7 text-muted sm:text-lg">
          Encuentra tu lugar para entrenar, elige un horario y reserva. Así de simple. Así de cerca.
        </p>
        <div className="mt-8 flex flex-wrap items-center gap-x-7 gap-y-5">
          <Link to="/register" className="inline-flex items-center gap-6 rounded-lg bg-accent px-6 py-4 text-sm font-bold transition-colors hover:bg-[#f15d28]">
            Empezar ahora <ArrowIcon />
          </Link>
          <a href="#como-funciona" className="inline-flex items-center gap-2 py-3 text-sm font-semibold underline underline-offset-4">
            Cómo funciona <span aria-hidden="true">↘</span>
          </a>
        </div>
        <p className="mt-7 text-xs text-muted">Elige tu momento para ir a entrenar.</p>
      </div>
      <div className="relative overflow-hidden rounded-2xl bg-ink">
        <img
          src="https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=1200&q=85"
          alt="Zona de entrenamiento con pesas y equipamiento de gimnasio"
          width={1200}
          height={1400}
          fetchPriority="high"
          className="aspect-[6/5] w-full object-cover brightness-75 lg:aspect-[6/7]"
        />
        <div className="absolute inset-0 bg-linear-to-t from-black/75 via-transparent to-transparent" aria-hidden="true" />
        <div className="absolute right-6 bottom-6 left-6 flex items-end justify-between gap-4 text-white sm:right-8 sm:bottom-8 sm:left-8">
          <p className="max-w-56 font-display text-4xl leading-none font-semibold uppercase sm:text-5xl">Un buen lugar.<br />Un nuevo comienzo.</p>
          <span aria-hidden="true" className="flex size-12 shrink-0 items-center justify-center rounded-full border border-white/60"><ArrowIcon className="size-6 -rotate-45" variant="white" /></span>
        </div>
      </div>
    </section>
  );
}
