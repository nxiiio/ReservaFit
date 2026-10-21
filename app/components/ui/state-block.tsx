import type { ReactNode } from "react";
import { AlertIcon } from "./icons";

type Props = {
  tone?: "empty" | "error";
  icon?: ReactNode;
  title: ReactNode;
  children?: ReactNode;
  action?: ReactNode;
  className?: string;
};

// Composed empty and error state used across the signed-in pages
export function StateBlock({ tone = "empty", icon, title, children, action, className = "" }: Props) {
  const isError = tone === "error";
  return (
    <div
      role={isError ? "alert" : undefined}
      className={`enter flex flex-col items-start gap-4 rounded-xl bg-surface px-6 py-7 sm:flex-row sm:items-center sm:gap-5 sm:px-7 ${className}`}
    >
      <span
        aria-hidden="true"
        className={`flex size-11 shrink-0 items-center justify-center rounded-full bg-paper ${isError ? "text-accent-deep" : "text-ink"}`}
      >
        {icon ?? <AlertIcon />}
      </span>
      <div className="flex-1 text-sm leading-6">
        <p className="font-semibold text-ink">{title}</p>
        {children && <div className="mt-0.5 text-muted">{children}</div>}
      </div>
      {action && <div className="shrink-0">{action}</div>}
    </div>
  );
}

export const retryButtonClass =
  "press flex h-10 cursor-pointer items-center justify-center rounded-lg border border-line-strong bg-white px-5 text-sm font-semibold text-ink hover:border-ink";

export const primaryButtonClass =
  "press flex h-10 cursor-pointer items-center justify-center rounded-lg bg-ink px-5 text-sm font-semibold text-paper hover:bg-ink/85 disabled:cursor-not-allowed disabled:opacity-50";
