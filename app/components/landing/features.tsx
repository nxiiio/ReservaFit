const features = [
  { number: "01", title: "Cerca de donde estás", description: "Busca por comuna y descubre gimnasios independientes que forman parte de tu barrio.", icon: "/icons/feature-location.svg" },
  { number: "02", title: "Tu horario, tu ritmo", description: "Revisa los horarios disponibles y reserva tu próximo entrenamiento en pocos pasos.", icon: "/icons/feature-clock.svg" },
  { number: "03", title: "Entrena y apoya lo local", description: "Elige un gimnasio independiente y ayuda a crecer a quienes mueven tu comunidad.", icon: "/icons/feature-home.svg" },
];

export function Features() {
  return (
    <section id="gimnasios" aria-labelledby="features-title" className="border-y border-line bg-[#f0f0e9]">
      <div className="mx-auto max-w-7xl px-5 py-12 sm:px-8 lg:px-12 lg:py-16">
        <div className="mb-9 flex flex-wrap items-end justify-between gap-4">
          <h2 id="features-title" className="font-display text-4xl font-semibold uppercase sm:text-5xl">Más cerca. Más simple. Más tuyo.</h2>
        </div>
        <div className="grid gap-8 md:grid-cols-3 md:gap-0">
          {features.map((feature, index) => (
            <article key={feature.number} className={`border-t border-line pt-6 md:border-t-0 md:pt-0 ${index > 0 ? "md:border-l md:pl-8" : ""} ${index < 2 ? "md:pr-8" : ""}`}>
              <div className="mb-5 flex items-center justify-between">
                <img aria-hidden="true" src={feature.icon} alt="" className="size-7" />
                <span className="text-xs font-medium text-muted">/ {feature.number}</span>
              </div>
              <h3 className="text-lg font-bold tracking-tight">{feature.title}</h3>
              <p className="mt-3 max-w-sm text-sm leading-6 text-muted">{feature.description}</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
