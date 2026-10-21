import { Link } from "react-router";
import { Brand } from "../landing/brand";
import { BackIcon } from "../ui/icons";

export function LoginHeader() {
  return (
    <header className="mx-auto flex w-full max-w-7xl flex-wrap items-center justify-between gap-5 px-5 py-6 sm:px-8 lg:px-12">
      <Brand />
      <Link to="/" className="group inline-flex items-center gap-2 py-2 text-sm font-semibold text-muted transition-colors hover:text-ink">
        <BackIcon className="size-4 transition-transform duration-200 ease-out-expo motion-safe:group-hover:-translate-x-0.5" /> Volver al inicio
      </Link>
    </header>
  );
}
