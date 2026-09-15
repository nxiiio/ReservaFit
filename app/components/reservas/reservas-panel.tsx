import { useEffect, useMemo, useState } from "react";
import { fetchGyms, type Gym } from "../../lib/api";
import { GymCard } from "./gym-card";

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
      <h1 className="font-display text-3xl font-bold sm:text-4xl">Gimnasios cerca de ti</h1>

      <label htmlFor="comuna" className="sr-only">
        Filtrar por comuna
      </label>
      <select
        id="comuna"
        value={comuna}
        onChange={(event) => setComuna(event.target.value)}
        disabled={gymsState.status !== "ready"}
        className="mt-6 h-11 w-full cursor-pointer rounded-md border border-muted/60 bg-white px-3 text-sm disabled:cursor-not-allowed disabled:opacity-60"
      >
        <option value="">Todas las comunas</option>
        {comunas.map((name) => (
          <option key={name} value={name}>
            {name}
          </option>
        ))}
      </select>

      {gymsState.status === "loading" && <p className="mt-8 text-sm text-muted">Cargando gimnasios…</p>}

      {gymsState.status === "error" && (
        <p className="mt-8 text-sm text-[#b94018]">
          No se pudieron cargar los gimnasios.{" "}
          <button type="button" onClick={() => setAttempt((n) => n + 1)} className="cursor-pointer font-semibold underline">
            Reintentar
          </button>
        </p>
      )}

      {gymsState.status === "ready" && visibleGyms.length === 0 && (
        <p className="mt-8 text-sm text-muted">No hay gimnasios para mostrar.</p>
      )}

      {visibleGyms.length > 0 && (
        <ul className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {visibleGyms.map((gym) => (
            <li key={gym.id} className="flex">
              <GymCard gym={gym} />
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
