import { useMsal } from "@azure/msal-react";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { createBooking, fetchAvailability, fetchGym } from "../../lib/api";
import { getErrorMessage, getErrorStatus } from "../../lib/api-errors";
import { acquireApiToken } from "../../lib/auth-token";
import type { Gym, SlotAvailability } from "../../types/gym";
import { addDaysIso, formatLongDate, formatTime, todayIso } from "../../lib/dates";

const MAX_DAYS_AHEAD = 30;

type GymState = { status: "loading" } | { status: "error" } | { status: "not-found" } | { status: "ready"; gym: Gym };
type SlotsState = { status: "loading" } | { status: "error" } | { status: "ready"; slots: SlotAvailability[] };

export function GymBookingPanel({ gymId }: { gymId: string }) {
  const { instance, accounts } = useMsal();
  const account = accounts[0];
  const navigate = useNavigate();
  const [today] = useState(todayIso);
  const [gymState, setGymState] = useState<GymState>({ status: "loading" });
  const [gymAttempt, setGymAttempt] = useState(0);
  const [date, setDate] = useState(today);
  const [slotsState, setSlotsState] = useState<SlotsState>({ status: "loading" });
  const [slotsAttempt, setSlotsAttempt] = useState(0);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [imageFailed, setImageFailed] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setGymState({ status: "loading" });
    fetchGym(gymId)
      .then((gym) => !cancelled && setGymState({ status: "ready", gym }))
      .catch((err) => {
        if (cancelled) return;
        setGymState(getErrorStatus(err) === 404 ? { status: "not-found" } : { status: "error" });
      });
    return () => {
      cancelled = true;
    };
  }, [gymId, gymAttempt]);

  useEffect(() => {
    let cancelled = false;
    setSlotsState({ status: "loading" });
    fetchAvailability(gymId, date)
      .then((slots) => !cancelled && setSlotsState({ status: "ready", slots }))
      .catch(() => !cancelled && setSlotsState({ status: "error" }));
    return () => {
      cancelled = true;
    };
  }, [gymId, date, slotsAttempt]);

  function handleDateChange(value: string) {
    // Cleared or partially typed input: keep the last valid date
    if (!value) return;
    setDate(value);
    setSelectedId(null);
    setBookingError(null);
  }

  async function handleConfirm() {
    if (selectedId === null || !account) return;
    setSubmitting(true);
    setBookingError(null);
    try {
      const token = await acquireApiToken(instance, account);
      if (!token) return;
      await createBooking(token, { scheduleId: selectedId, date });
      navigate("/mis-reservas", { state: { justBooked: true } });
    } catch (err) {
      console.error(err);
      const status = getErrorStatus(err);
      setBookingError(getErrorMessage(err) ?? "No se pudo crear la reserva. Intenta de nuevo.");
      if (status === 409 || status === 400) {
        // Someone else took it, or it just started: show fresh availability
        setSelectedId(null);
        setSlotsAttempt((n) => n + 1);
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (gymState.status === "not-found") {
    return (
      <section className="mx-auto w-full max-w-7xl px-5 py-10 sm:px-8 lg:px-12">
        <h1 className="font-display text-3xl font-bold sm:text-4xl">Gimnasio no encontrado</h1>
        <p className="mt-3 text-sm text-muted">
          Puede que ya no esté disponible.{" "}
          <Link to="/reservas" className="font-semibold text-ink underline">
            Volver a gimnasios
          </Link>
        </p>
      </section>
    );
  }

  const gym = gymState.status === "ready" ? gymState.gym : null;
  const showImage = gym?.imageUrl && !imageFailed;
  const selectedSlot =
    slotsState.status === "ready" ? (slotsState.slots.find((slot) => slot.id === selectedId) ?? null) : null;

  return (
    <section className="mx-auto w-full max-w-7xl px-5 py-10 sm:px-8 lg:px-12">
      <Link to="/reservas" className="text-sm font-medium text-muted transition-colors hover:text-ink">
        ← Volver a gimnasios
      </Link>

      {gymState.status === "loading" && <p className="mt-6 text-sm text-muted">Cargando gimnasio…</p>}

      {gymState.status === "error" && (
        <p className="mt-6 text-sm text-[#b94018]">
          No se pudo cargar el gimnasio.{" "}
          <button type="button" onClick={() => setGymAttempt((n) => n + 1)} className="cursor-pointer font-semibold underline">
            Reintentar
          </button>
        </p>
      )}

      {gym && (
        <header className="mt-6 grid gap-6 overflow-hidden rounded-lg border border-line bg-white md:grid-cols-[2fr_3fr]">
          <div className="flex aspect-[16/9] items-center justify-center bg-line/70 md:aspect-auto md:min-h-56">
            {showImage ? (
              <img
                src={gym.imageUrl!}
                alt={`Foto de ${gym.name}`}
                onError={() => setImageFailed(true)}
                className="size-full object-cover"
              />
            ) : (
              <svg aria-hidden="true" viewBox="0 0 24 24" className="size-7 text-muted" fill="none" stroke="currentColor" strokeWidth="1.6">
                <rect x="3" y="4" width="18" height="16" rx="2" />
                <circle cx="9" cy="10" r="2" />
                <path d="m21 16-5-5-9 9" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            )}
          </div>
          <div className="px-5 pb-5 md:py-6 md:pr-6 md:pl-0">
            <h1 className="font-display text-3xl font-bold sm:text-4xl">{gym.name}</h1>
            {(gym.comuna || gym.address) && (
              <p className="mt-2 text-sm text-muted">{[gym.address, gym.comuna].filter(Boolean).join(", ")}</p>
            )}
            {gym.description && <p className="mt-4 text-sm leading-6">{gym.description}</p>}
          </div>
        </header>
      )}

      <div className="mt-10">
        <h2 className="font-display text-2xl font-semibold">Elige un horario</h2>

        <label htmlFor="booking-date" className="mt-4 block text-sm font-medium">
          Fecha
        </label>
        <input
          id="booking-date"
          type="date"
          value={date}
          min={today}
          max={addDaysIso(today, MAX_DAYS_AHEAD)}
          onChange={(event) => handleDateChange(event.target.value)}
          className="mt-2 h-11 w-full max-w-xs cursor-pointer rounded-md border border-muted/60 bg-white px-3 text-sm"
        />
        <p className="mt-2 text-sm text-muted first-letter:uppercase">{formatLongDate(date)}</p>

        {slotsState.status === "loading" && <p className="mt-6 text-sm text-muted">Cargando horarios…</p>}

        {slotsState.status === "error" && (
          <p className="mt-6 text-sm text-[#b94018]">
            No se pudieron cargar los horarios.{" "}
            <button type="button" onClick={() => setSlotsAttempt((n) => n + 1)} className="cursor-pointer font-semibold underline">
              Reintentar
            </button>
          </p>
        )}

        {slotsState.status === "ready" && slotsState.slots.length === 0 && (
          <p className="mt-6 text-sm text-muted">Este gimnasio no tiene horarios publicados.</p>
        )}

        {slotsState.status === "ready" && slotsState.slots.length > 0 && (
          <ul className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
            {slotsState.slots.map((slot) => {
              const selected = slot.id === selectedId;
              return (
                <li key={slot.id}>
                  <button
                    type="button"
                    disabled={!slot.available || submitting}
                    aria-pressed={selected}
                    onClick={() => {
                      setSelectedId(slot.id);
                      setBookingError(null);
                    }}
                    className={`flex h-16 w-full cursor-pointer flex-col items-center justify-center rounded-md border text-sm transition-colors disabled:cursor-not-allowed ${
                      selected
                        ? "border-ink bg-ink text-white"
                        : slot.available
                          ? "border-line bg-white hover:border-ink"
                          : "border-line bg-line/40 text-muted"
                    }`}
                  >
                    <span className="font-semibold">
                      {formatTime(slot.startTime)} – {formatTime(slot.endTime)}
                    </span>
                    {!slot.available && <span className="text-xs">Ocupado</span>}
                  </button>
                </li>
              );
            })}
          </ul>
        )}

        {bookingError && (
          <p className="mt-6 text-sm text-[#b94018]" role="alert">
            {bookingError}
          </p>
        )}

        <div className="mt-6 flex flex-wrap items-center gap-4">
          <button
            type="button"
            onClick={handleConfirm}
            disabled={!selectedSlot || submitting || !gym}
            className="flex h-11 cursor-pointer items-center justify-center rounded-md bg-ink px-6 text-sm font-semibold text-white transition-colors hover:bg-ink/85 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting ? "Reservando…" : "Confirmar reserva"}
          </button>
          {selectedSlot && (
            <p className="text-sm text-muted">
              {formatTime(selectedSlot.startTime)} – {formatTime(selectedSlot.endTime)}, {formatLongDate(date)}
            </p>
          )}
        </div>
      </div>
    </section>
  );
}
