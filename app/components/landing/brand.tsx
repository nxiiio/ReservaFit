import { Link } from "react-router";

export function Brand() {
  return (
    <Link to="/" aria-label="ReservaFit, inicio" className="inline-flex items-center gap-2.5 text-xl font-bold tracking-tight">
      <span aria-hidden="true" className="flex size-8 items-center justify-center rounded-lg bg-accent text-ink">
        <img src="/icons/brand-mark.svg" alt="" className="size-5" />
      </span>
      ReservaFit<span aria-hidden="true" className="-ml-2 text-accent">.</span>
    </Link>
  );
}
