import { useReveal } from "../../hooks/use-reveal";

const features = [
  { title: "Cerca de donde estás", description: "Busca por comuna y descubre gimnasios independientes que forman parte de tu barrio.", icon: "/icons/feature-location.svg" },
  { title: "Tu horario, tu ritmo", description: "Revisa los horarios disponibles y reserva tu próximo entrenamiento en pocos pasos.", icon: "/icons/feature-clock.svg" },
  { title: "Entrena y apoya lo local", description: "Elige un gimnasio independiente y ayuda a crecer a quienes mueven tu comunidad.", icon: "/icons/feature-home.svg" },
];

export function Features() {
  const ref = useReveal<HTMLDivElement>();
  const [lead, ...rest] = features;

  return (
    <section id="gimnasios" aria-labelledby="features-title" className="border-y border-line bg-surface">
      <div ref={ref} className="mx-auto max-w-7xl px-5 py-16 sm:px-8 lg:px-12 lg:py-24">
        <h2 id="features-title" className="reveal max-w-3xl font-display text-4xl leading-[0.95] font-semibold uppercase sm:text-6xl">Más cerca. Más simple. Más tuyo.</h2>
        {/* One lead feature on a raised surface, two supporting ones beside it */}
        <div className="mt-10 grid gap-8 md:grid-cols-[1.15fr_1fr] md:gap-12 lg:mt-14">
          <article className="reveal flex flex-col justify-between gap-12 rounded-2xl bg-paper p-7 shadow-card sm:p-10" style={{ "--i": 1 } as React.CSSProperties}>
            <img aria-hidden="true" src={lead.icon} alt="" className="size-10" />
            <div>
              <h3 className="font-display text-3xl leading-none font-semibold uppercase sm:text-4xl">{lead.title}</h3>
              <p className="mt-4 max-w-sm text-base leading-7 text-muted">{lead.description}</p>
            </div>
          </article>
          <div className="flex flex-col justify-center divide-y divide-line">
            {rest.map((feature, index) => (
              <article key={feature.title} className="reveal flex gap-5 py-7 first:pt-0 last:pb-0 md:first:pt-2" style={{ "--i": index + 2 } as React.CSSProperties}>
                <img aria-hidden="true" src={feature.icon} alt="" className="mt-0.5 size-7 shrink-0" />
                <div>
                  <h3 className="text-lg font-bold tracking-tight">{feature.title}</h3>
                  <p className="mt-2 max-w-sm text-sm leading-6 text-muted">{feature.description}</p>
                </div>
              </article>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
