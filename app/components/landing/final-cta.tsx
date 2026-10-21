import { Link } from "react-router";
import { useReveal } from "../../hooks/use-reveal";
import { ArrowIcon } from "./arrow-icon";

export function FinalCta() {
  const ref = useReveal<HTMLDivElement>();

  return (
    <section aria-labelledby="cta-title" className="mx-auto w-full max-w-7xl px-5 pb-20 sm:px-8 lg:px-12">
      <div ref={ref} className="reveal-scale flex flex-col items-start justify-between gap-10 rounded-2xl bg-ink px-7 py-12 text-paper sm:px-12 sm:py-16 md:flex-row md:items-end">
        <div>
          <p className="mb-5 text-xs font-semibold tracking-widest text-accent uppercase">Tu próximo paso empieza aquí</p>
          <h2 id="cta-title" className="font-display text-[clamp(2.4rem,8vw,4.5rem)] leading-[0.92] font-semibold uppercase">Hazle espacio<br />a tu entrenamiento.</h2>
          <p className="mt-6 max-w-md text-sm leading-6 text-paper/75">Tu comunidad se mueve. Encuentra tu lugar en ella.</p>
        </div>
        <Link to="/login" className="press group inline-flex shrink-0 items-center gap-8 rounded-lg bg-accent px-6 py-4 text-sm font-bold text-ink hover:bg-accent-hover">Empezar ahora <ArrowIcon className="nudge size-4" /></Link>
      </div>
    </section>
  );
}
