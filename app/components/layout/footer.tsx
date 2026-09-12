import { Brand } from "../landing/brand";

export function Footer() {
  return (
    <footer className="border-t border-line">
      <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-5 px-5 py-7 sm:px-8 lg:px-12">
        <Brand />
        <p className="text-xs text-muted">© 2026 ReservaFit. Todos los derechos reservados.</p>
      </div>
    </footer>
  );
}
