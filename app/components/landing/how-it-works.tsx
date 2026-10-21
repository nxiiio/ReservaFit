import { useReveal } from "../../hooks/use-reveal";

const steps = [
  { title: "Crea tu cuenta", description: "Regístrate y agrega un método de pago ficticio. Es una simulación: no se realiza ningún cobro real." },
  { title: "Encuentra tu horario", description: "Busca un gimnasio en tu comuna y elige un horario disponible que se ajuste a tu día." },
  { title: "Confirma y a entrenar", description: "Revisa los detalles y confirma tu reserva. Tu próximo entrenamiento ya tiene lugar y hora." },
];

export function HowItWorks() {
  const ref = useReveal<HTMLElement>();

  return (
    <section ref={ref} id="como-funciona" aria-labelledby="steps-title" className="mx-auto grid max-w-7xl gap-10 px-5 py-20 sm:px-8 lg:grid-cols-[1fr_1.15fr] lg:gap-24 lg:px-12 lg:py-28">
      <div className="reveal lg:sticky lg:top-12 lg:self-start">
        <p className="mb-4 text-xs font-bold tracking-[0.16em] uppercase text-muted">Cómo funciona</p>
        <h2 id="steps-title" className="max-w-md font-display text-5xl leading-[0.95] font-semibold uppercase sm:text-6xl">De las ganas<br />al entrenamiento.<br /><span className="text-accent-deep">En 3 pasos.</span></h2>
        <p className="mt-6 max-w-xs text-sm leading-6 text-muted">Menos vueltas para reservar.<br />Más energía para lo que te gusta.</p>
      </div>
      <ol className="divide-y divide-line border-t border-line">
        {steps.map((step, index) => (
          <li key={step.title} className="reveal-x flex gap-6 py-8 sm:gap-8" style={{ "--i": index + 1 } as React.CSSProperties}>
            <span aria-hidden="true" className="w-10 shrink-0 font-display text-5xl leading-none font-semibold text-accent-deep tabular-nums">0{index + 1}</span>
            <div>
              <h3 className="text-lg font-bold">{step.title}</h3>
              <p className="mt-2 max-w-md text-sm leading-6 text-muted">{step.description}</p>
            </div>
          </li>
        ))}
      </ol>
    </section>
  );
}
