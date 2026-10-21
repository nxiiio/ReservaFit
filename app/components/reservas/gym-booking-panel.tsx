import { useMsal } from "@azure/msal-react";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { createBooking, fetchAvailability, fetchGym } from "../../lib/api";
import { getErrorMessage, getErrorStatus } from "../../lib/api-errors";
import { acquireApiToken } from "../../lib/auth-token";
import { requestNotificationsRefresh } from "../../lib/notification-events";
import type { Gym, SlotAvailability } from "../../types/gym";
import { addDaysIso, formatLongDate, formatTime, todayIso } from "../../lib/dates";
import { BackIcon, CalendarIcon, CheckIcon, ImageIcon, SearchIcon } from "../ui/icons";
import { StateBlock, primaryButtonClass, retryButtonClass } from "../ui/state-block";

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
      requestNotificationsRefresh();
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
        <BackLink />
        <StateBlock
          className="mt-6"
          icon={<SearchIcon />}
          title={<span className="font-display text-2xl leading-tight font-bold">Gimnasio no encontrado</span>}
          action={
            <Link to="/reservas" className={primaryButtonClass}>
              Volver a gimnasios
            </Link>
          }
        >
          Puede que ya no esté disponible.
        </StateBlock>
      </section>
    );
  }

  const gym = gymState.status === "ready" ? gymState.gym : null;
  const showImage = gym?.imageUrl && !imageFailed;
  const selectedSlot =
    slotsState.status === "ready" ? (slotsState.slots.find((slot) => slot.id === selectedId) ?? null) : null;

  return (
    <section className="mx-auto w-full max-w-7xl px-5 py-10 sm:px-8 lg:px-12">
      <BackLink />

      {gymState.status === "loading" && (
        <div role="status" className="mt-6 grid overflow-hidden rounded-xl bg-white shadow-card md:grid-cols-[2fr_3fr]">
          <span className="sr-only">Cargando gimnasio…</span>
          <div aria-hidden="true" className="skeleton aspect-[16/9] md:aspect-auto md:min-h-56" />
          <div aria-hidden="true" className="p-5 md:p-7">
            <div className="skeleton h-9 w-1/2 rounded-md" />
            <div className="skeleton mt-3 h-4 w-1/3 rounded-md" />
            <div className="skeleton mt-6 h-4 w-full rounded-md" />
            <div className="skeleton mt-2 h-4 w-4/5 rounded-md" />
          </div>
        </div>
      )}

      {gymState.status === "error" && (
        <StateBlock
          tone="error"
          className="mt-6"
          title="No se pudo cargar el gimnasio."
          action={
            <button type="button" onClick={() => setGymAttempt((n) => n + 1)} className={retryButtonClass}>
              Reintentar
            </button>
          }
        />
      )}

      {gym && (
        <header className="enter mt-6 grid overflow-hidden rounded-xl bg-white shadow-card md:grid-cols-[2fr_3fr]">
          <div className="flex aspect-[16/9] items-center justify-center bg-line/70 md:aspect-auto md:min-h-56">
            {showImage ? (
              <img
                src={gym.imageUrl!}
                alt={`Foto de ${gym.name}`}
                onError={() => setImageFailed(true)}
                className="size-full object-cover"
              />
            ) : (
              <ImageIcon className="size-7 text-muted" />
            )}
          </div>
          <div className="p-5 md:p-7">
            <h1 className="font-display text-4xl leading-none font-bold sm:text-5xl">{gym.name}</h1>
            {(gym.comuna || gym.address) && (
              <p className="mt-2.5 text-sm text-muted">{[gym.address, gym.comuna].filter(Boolean).join(", ")}</p>
            )}
            {gym.description && <p className="mt-4 max-w-prose text-sm leading-6">{gym.description}</p>}
          </div>
        </header>
      )}

      <div className="mt-12">
        <div className="flex flex-col gap-5 border-b border-line pb-6 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="font-display text-3xl leading-none font-semibold">Elige un horario</h2>
            <p className="mt-2 text-sm text-muted first-letter:uppercase">{formatLongDate(date)}</p>
          </div>
          <div className="flex flex-col gap-2">
            <label htmlFor="booking-date" className="text-sm font-semibold">
              Fecha
            </label>
            <div className="relative">
              <CalendarIcon className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted" />
              <input
                id="booking-date"
                type="date"
                value={date}
                min={today}
                max={addDaysIso(today, MAX_DAYS_AHEAD)}
                onChange={(event) => handleDateChange(event.target.value)}
                className="h-11 w-full cursor-pointer rounded-lg border border-line-strong bg-white pr-3 pl-9 text-sm text-ink transition-colors hover:border-ink sm:w-60"
              />
            </div>
          </div>
        </div>

        {slotsState.status === "loading" && (
          <div role="status" className="mt-6">
            <span className="sr-only">Cargando horarios…</span>
            <ul aria-hidden="true" className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
              {Array.from({ length: 10 }, (_, index) => (
                <li key={index} className="skeleton h-16 rounded-lg" />
              ))}
            </ul>
          </div>
        )}

        {slotsState.status === "error" && (
          <StateBlock
            tone="error"
            className="mt-6"
            title="No se pudieron cargar los horarios."
            action={
              <button type="button" onClick={() => setSlotsAttempt((n) => n + 1)} className={retryButtonClass}>
                Reintentar
              </button>
            }
          />
        )}

        {slotsState.status === "ready" && slotsState.slots.length === 0 && (
          <StateBlock className="mt-6" icon={<CalendarIcon />} title="Este gimnasio no tiene horarios publicados." />
        )}

        {slotsState.status === "ready" && slotsState.slots.length > 0 && (
          <ul className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
            {slotsState.slots.map((slot, index) => {
              const selected = slot.id === selectedId;
              return (
                <li key={slot.id} className="enter" style={{ "--i": Math.min(index, 8) } as React.CSSProperties}>
                  <button
                    type="button"
                    disabled={!slot.available || submitting}
                    aria-pressed={selected}
                    onClick={() => {
                      setSelectedId(slot.id);
                      setBookingError(null);
                    }}
                    className={`press relative flex h-16 w-full cursor-pointer flex-col items-center justify-center rounded-lg border text-sm tabular-nums disabled:cursor-not-allowed ${
                      selected
                        ? "border-ink bg-ink text-paper"
                        : slot.available
                          ? "border-line-strong bg-white text-ink hover:border-ink hover:bg-surface disabled:opacity-50"
                          : "border-dashed border-line-strong bg-transparent text-muted"
                    }`}
                  >
                    {selected && <CheckIcon className="enter absolute top-2 right-2 size-4 text-accent" />}
                    <span className={`font-semibold ${slot.available ? "" : "line-through decoration-1"}`}>
                      {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
                    </span>
                    {!slot.available && <span className="text-xs">Ocupado</span>}
                  </button>
                </li>
              );
            })}
          </ul>
        )}

        {bookingError && (
          <p className="enter mt-6 text-sm text-accent-deep" role="alert">
            {bookingError}
          </p>
        )}

        <div className="mt-8 flex flex-wrap items-center gap-x-5 gap-y-3 border-t border-line pt-6">
          <button
            type="button"
            onClick={handleConfirm}
            disabled={!selectedSlot || submitting || !gym}
            className="press flex h-12 cursor-pointer items-center justify-center rounded-lg bg-ink px-7 text-sm font-semibold text-paper hover:bg-ink/85 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting ? "Reservando…" : "Confirmar reserva"}
          </button>
          {selectedSlot && (
            <p key={selectedSlot.id} className="enter-x text-sm text-ink tabular-nums">
              {formatTime(selectedSlot.startTime)} - {formatTime(selectedSlot.endTime)}, {formatLongDate(date)}
            </p>
          )}
        </div>
      </div>
    </section>
  );
}

function BackLink() {
  return (
    <Link to="/reservas" className="group inline-flex items-center gap-2 text-sm font-medium text-muted transition-colors hover:text-ink">
      <BackIcon className="size-4 transition-transform duration-200 ease-out-expo motion-safe:group-hover:-translate-x-0.5" />
      Volver a gimnasios
    </Link>
  );
}
