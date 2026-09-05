const features = [
  { number: "01", title: "Cerca de donde estás", description: "Busca por comuna y descubre gimnasios independientes que forman parte de tu barrio.", path: "M20 10c0 6-8 11-8 11S4 16 4 10a8 8 0 1 1 16 0ZM12 7a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z" },
  { number: "02", title: "Tu horario, tu ritmo", description: "Revisa los horarios disponibles y reserva tu próximo entrenamiento en pocos pasos.", path: "M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0ZM12 7v5l3 2" },
  { number: "03", title: "Entrena y apoya lo local", description: "Elige un gimnasio independiente y ayuda a crecer a quienes mueven tu comunidad.", path: "m3 10 9-7 9 7v11H3V10Zm6 11v-8h6v8M1 10l11-9 11 9" },
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
                <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round" className="size-7"><path d={feature.path} /></svg>
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
