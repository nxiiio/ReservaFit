import { Link } from "react-router";

export function Brand() {
  return (
    <Link to="/" aria-label="ReservaFit, inicio" className="inline-flex items-center gap-2.5 text-xl font-bold tracking-tight">
      <span aria-hidden="true" className="flex size-8 items-center justify-center rounded-lg bg-accent text-ink">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className="size-5">
          <path d="M8 5 4 19M15 5l-4 14M6 12h13M20 5l-2 7" />
        </svg>
      </span>
      ReservaFit<span aria-hidden="true" className="-ml-2 text-accent">.</span>
    </Link>
  );
}
