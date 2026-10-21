export function LoginVisual() {
  return (
    <aside aria-label="Entrena cerca de ti" className="relative hidden min-h-[650px] overflow-hidden rounded-2xl bg-ink lg:block">
      <img
        src="https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=1200&q=85"
        alt=""
        width={1200}
        height={1400}
        className="settle absolute inset-0 h-full w-full object-cover brightness-60"
      />
      <div aria-hidden="true" className="absolute inset-0 bg-linear-to-t from-black/90 via-black/10 to-black/25" />
      <div className="relative flex h-full flex-col justify-end p-10 text-white xl:p-12">
        <div className="rise" style={{ "--i": 2 } as React.CSSProperties}>
          <span aria-hidden="true" className="mb-7 block h-1 w-12 rounded-full bg-accent" />
          <h2 className="font-display text-[clamp(4rem,5.5vw,5.5rem)] leading-[0.95] font-bold tracking-[-0.02em] uppercase">
            Tu momento<br />empieza<br /><span className="text-accent">acá.</span>
          </h2>
          <p className="mt-6 max-w-xs text-sm leading-6 text-white/80">
            Un gimnasio en tu barrio. Un horario que te acomoda. Un espacio para ti.
          </p>
        </div>
      </div>
    </aside>
  );
}
