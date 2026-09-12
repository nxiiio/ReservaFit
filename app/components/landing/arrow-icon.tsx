export function ArrowIcon({ className = "size-4", variant = "ink" }: { className?: string; variant?: "ink" | "white" }) {
  return <img aria-hidden="true" src={variant === "white" ? "/icons/arrow-icon-white.svg" : "/icons/arrow-icon.svg"} alt="" className={className} />;
}
