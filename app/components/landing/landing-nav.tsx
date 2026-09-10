import { Link } from "react-router";
import { Brand } from "./brand";

export function LandingNav() {
  return (
    <header className="border-b border-line">
      <nav aria-label="Navegación principal" className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-x-4 gap-y-5 px-5 py-5 sm:px-8 lg:px-12 lg:py-6">
        <Brand />
        <div className="order-3 flex w-full items-center justify-center gap-8 border-t border-line pt-4 text-sm font-medium sm:order-none sm:w-auto sm:border-0 sm:pt-0">
          <a href="#gimnasios" className="py-1 hover:underline underline-offset-4">Gimnasios</a>
          <a href="#como-funciona" className="py-1 hover:underline underline-offset-4">Cómo funciona</a>
        </div>
        <div className="flex items-center gap-3 text-xs font-semibold sm:gap-5 sm:text-sm">
          <Link to="/login" className="rounded-lg bg-ink px-3 py-3 text-white transition-colors hover:bg-ink/85 sm:px-5">Ingresar</Link>
        </div>
      </nav>
    </header>
  );
}
