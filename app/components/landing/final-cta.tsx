import { Link } from "react-router";
import { ArrowIcon } from "./arrow-icon";

export function FinalCta() {
  return (
    <section aria-labelledby="cta-title" className="mx-auto max-w-7xl px-5 pb-16 sm:px-8 lg:px-12">
      <div className="flex flex-col items-start justify-between gap-8 rounded-2xl bg-ink px-7 py-10 text-paper sm:px-12 sm:py-12 md:flex-row md:items-center">
        <div>
          <p className="mb-4 text-xs font-semibold tracking-widest text-accent uppercase">Tu próximo paso empieza aquí</p>
          <h2 id="cta-title" className="font-display text-[clamp(2rem,8vw,3rem)] leading-none font-semibold uppercase sm:text-6xl">Hazle espacio<br />a tu entrenamiento.</h2>
          <p className="mt-5 max-w-md text-sm leading-6 text-paper/75">Tu comunidad se mueve. Encuentra tu lugar en ella.</p>
        </div>
        <Link to="/register" className="inline-flex shrink-0 items-center gap-8 rounded-lg bg-accent px-6 py-4 text-sm font-bold text-ink transition-colors hover:bg-[#ff8355]">Crear mi cuenta <ArrowIcon /></Link>
      </div>
    </section>
  );
}
