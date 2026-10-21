import { useEffect, useRef } from "react";

const TARGETS = ".reveal, .reveal-x, .reveal-scale";

// Reveals the container's .reveal* elements once it scrolls into view.
// Content stays visible when JS, IntersectionObserver or motion is unavailable:
// targets are only hidden ("pending") after this effect runs.
export function useReveal<T extends HTMLElement>() {
  const ref = useRef<T>(null);

  useEffect(() => {
    const container = ref.current;
    if (!container || typeof IntersectionObserver === "undefined") return;
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

    const targets = [
      ...(container.matches(TARGETS) ? [container] : []),
      ...container.querySelectorAll<HTMLElement>(TARGETS),
    ];
    for (const target of targets) target.dataset.reveal = "pending";

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (!entry.isIntersecting) return;
        for (const target of targets) target.dataset.reveal = "shown";
        observer.disconnect();
      },
      { threshold: 0.15, rootMargin: "0px 0px -8% 0px" },
    );
    observer.observe(container);

    return () => {
      observer.disconnect();
      for (const target of targets) delete target.dataset.reveal;
    };
  }, []);

  return ref;
}
