import { useEffect, useMemo, useState } from "react";
import { fetchGyms } from "../../lib/api";
import type { Gym } from "../../types/gym";
import { GymCard, GymCardSkeleton } from "./gym-card";
import { SearchIcon } from "../ui/icons";
import { StateBlock, retryButtonClass } from "../ui/state-block";

type GymsState = { status: "loading" } | { status: "error" } | { status: "ready"; gyms: Gym[] };

export function ReservasPanel() {
  const [gymsState, setGymsState] = useState<GymsState>({ status: "loading" });
  const [comuna, setComuna] = useState("");
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setGymsState({ status: "loading" });
    fetchGyms()
      .then((gyms) => !cancelled && setGymsState({ status: "ready", gyms }))
      .catch(() => !cancelled && setGymsState({ status: "error" }));
    return () => {
      cancelled = true;
    };
  }, [attempt]);

  const comunas = useMemo(() => {
    if (gymsState.status !== "ready") return [];
    const names = gymsState.gyms.map((gym) => gym.comuna).filter((c): c is string => Boolean(c));
    return [...new Set(names)].sort((a, b) => a.localeCompare(b, "es"));
  }, [gymsState]);

  const visibleGyms =
    gymsState.status === "ready" ? gymsState.gyms.filter((gym) => !comuna || gym.comuna === comuna) : [];

  return (
    <section className="mx-auto w-full max-w-7xl px-5 py-10 sm:px-8 lg:px-12">
      <h1 className="font-display text-4xl leading-none font-bold sm:text-5xl">Gimnasios cerca de ti</h1>

      <label htmlFor="comuna" className="sr-only">
        Filtrar por comuna
      </label>
      <select
        id="comuna"
        value={comuna}
        onChange={(event) => setComuna(event.target.value)}
        disabled={gymsState.status !== "ready"}
        className="mt-6 h-11 w-full cursor-pointer rounded-lg border border-line-strong bg-white px-3 text-sm text-ink transition-colors hover:border-ink disabled:cursor-not-allowed disabled:opacity-60 sm:max-w-xs"
      >
        <option value="">Todas las comunas</option>
        {comunas.map((name) => (
          <option key={name} value={name}>
            {name}
          </option>
        ))}
      </select>

      {gymsState.status === "loading" && (
        <div className="mt-8" role="status">
          <span className="sr-only">Cargando gimnasios…</span>
          <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }, (_, index) => (
              <li key={index} className="flex">
                <GymCardSkeleton />
              </li>
            ))}
          </ul>
        </div>
      )}

      {gymsState.status === "error" && (
        <StateBlock
          tone="error"
          className="mt-8"
          title="No se pudieron cargar los gimnasios."
          action={
            <button type="button" onClick={() => setAttempt((n) => n + 1)} className={retryButtonClass}>
              Reintentar
            </button>
          }
        />
      )}

      {gymsState.status === "ready" && visibleGyms.length === 0 && (
        <StateBlock className="mt-8" icon={<SearchIcon />} title="No hay gimnasios para mostrar." />
      )}

      {visibleGyms.length > 0 && (
        // key: replay the short entrance when the comuna filter changes the list
        <ul key={comuna} className="mt-8 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {visibleGyms.map((gym, index) => (
            <li key={gym.id} className="enter flex" style={{ "--i": Math.min(index, 8) } as React.CSSProperties}>
              <GymCard gym={gym} />
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
