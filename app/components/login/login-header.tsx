import { Link } from "react-router";
import { Brand } from "../landing/brand";

export function LoginHeader() {
  return (
    <header className="mx-auto flex w-full max-w-7xl flex-wrap items-center justify-between gap-5 px-5 py-6 sm:px-8 lg:px-12">
      <Brand />
      <Link to="/" className="inline-flex items-center gap-2 py-2 text-sm font-semibold text-muted transition-colors hover:text-ink">
        <span aria-hidden="true">←</span> Volver al inicio
      </Link>
    </header>
  );
}
