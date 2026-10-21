import { useMsal } from "@azure/msal-react";
import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router";
import { cancelBooking, fetchMyBookings } from "../../lib/api";
import { getErrorMessage } from "../../lib/api-errors";
import { acquireApiToken } from "../../lib/auth-token";
import { requestNotificationsRefresh } from "../../lib/notification-events";
import type { Booking } from "../../types/booking";
import { formatLongDate, formatTime, todayIso } from "../../lib/dates";
import { CalendarIcon, CheckIcon } from "../ui/icons";
import { StateBlock, primaryButtonClass, retryButtonClass } from "../ui/state-block";

type BookingsState = { status: "loading" } | { status: "error" } | { status: "ready"; bookings: Booking[] };

type DisplayStatus = "confirmed" | "cancelled" | "finished";

const STATUS_LABELS: Record<DisplayStatus, string> = {
  confirmed: "Confirmada",
  cancelled: "Cancelada",
  finished: "Finalizada",
};

const STATUS_STYLES: Record<DisplayStatus, string> = {
  confirmed: "bg-ink text-paper",
  cancelled: "border border-line text-muted",
  finished: "bg-line text-ink",
};

function displayStatus(booking: Booking, today: string): DisplayStatus {
  if (booking.status === "CANCELADA") return "cancelled";
  // ISO dates compare correctly as strings
  return booking.date < today ? "finished" : "confirmed";
}

export function MyBookingsPanel() {
  const { instance, accounts } = useMsal();
  const account = accounts[0];
  const location = useLocation();
  const justBooked = Boolean((location.state as { justBooked?: boolean } | null)?.justBooked);
  const [today] = useState(todayIso);
  const [state, setState] = useState<BookingsState>({ status: "loading" });
  const [attempt, setAttempt] = useState(0);
  const [confirmingId, setConfirmingId] = useState<number | null>(null);
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [cancelErrors, setCancelErrors] = useState<Record<number, string>>({});

  useEffect(() => {
    if (!account) return;
    let cancelled = false;
    setState({ status: "loading" });

    (async () => {
      try {
        const token = await acquireApiToken(instance, account);
        if (!token) return;
        const bookings = await fetchMyBookings(token);
        if (!cancelled) setState({ status: "ready", bookings });
      } catch (err) {
        console.error(err);
        if (!cancelled) setState({ status: "error" });
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [account, instance, attempt]);

  async function handleCancel(id: number) {
    if (!account) return;
    setCancellingId(id);
    setCancelErrors(({ [id]: _, ...rest }) => rest);
    try {
      const token = await acquireApiToken(instance, account);
      if (!token) return;
      const updated = await cancelBooking(token, id);
      requestNotificationsRefresh();
      setState((current) =>
        current.status === "ready"
          ? { status: "ready", bookings: current.bookings.map((b) => (b.id === id ? updated : b)) }
          : current,
      );
    } catch (err) {
      console.error(err);
      const message = getErrorMessage(err) ?? "No se pudo cancelar la reserva. Intenta de nuevo.";
      setCancelErrors((errors) => ({ ...errors, [id]: message }));
    } finally {
      setCancellingId(null);
      setConfirmingId(null);
    }
  }

  return (
    <section className="mx-auto w-full max-w-7xl px-5 py-10 sm:px-8 lg:px-12">
      <h1 className="font-display text-4xl leading-none font-bold sm:text-5xl">Mis reservas</h1>

      {justBooked && (
        <p className="enter mt-6 flex items-center gap-3 rounded-xl bg-white px-4 py-3 text-sm font-medium shadow-card" role="status">
          <span aria-hidden="true" className="flex size-7 shrink-0 items-center justify-center rounded-full bg-accent text-ink">
            <CheckIcon className="size-4" />
          </span>
          ¡Reserva confirmada! Te esperamos.
        </p>
      )}

      {state.status === "loading" && (
        <div role="status" className="mt-8">
          <span className="sr-only">Cargando reservas…</span>
          <ul aria-hidden="true" className="flex flex-col gap-4">
            {Array.from({ length: 3 }, (_, index) => (
              <li key={index} className="flex flex-col gap-4 rounded-xl bg-white p-5 shadow-card sm:flex-row sm:items-center sm:justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3">
                    <div className="skeleton h-6 w-44 rounded-md" />
                    <div className="skeleton h-5 w-20 rounded-full" />
                  </div>
                  <div className="skeleton mt-3 h-4 w-24 rounded-md" />
                  <div className="skeleton mt-3 h-4 w-64 max-w-full rounded-md" />
                </div>
                <div className="skeleton h-10 w-28 rounded-lg" />
              </li>
            ))}
          </ul>
        </div>
      )}

      {state.status === "error" && (
        <StateBlock
          tone="error"
          className="mt-8"
          title="No se pudieron cargar tus reservas."
          action={
            <button type="button" onClick={() => setAttempt((n) => n + 1)} className={retryButtonClass}>
              Reintentar
            </button>
          }
        />
      )}

      {state.status === "ready" && state.bookings.length === 0 && (
        <StateBlock
          className="mt-8"
          icon={<CalendarIcon />}
          title="Aún no tienes reservas."
          action={
            <Link to="/reservas" className={primaryButtonClass}>
              Ver gimnasios
            </Link>
          }
        >
          Elige un gimnasio y reserva tu próximo entrenamiento.
        </StateBlock>
      )}

      {state.status === "ready" && state.bookings.length > 0 && (
        <ul className="mt-8 flex flex-col gap-4">
          {state.bookings.map((booking, index) => {
            const status = displayStatus(booking, today);
            const confirming = confirmingId === booking.id;
            const cancelling = cancellingId === booking.id;
            return (
              <li
                key={booking.id}
                style={{ "--i": Math.min(index, 8) } as React.CSSProperties}
                className={`enter flex flex-col gap-4 rounded-xl p-5 transition-colors duration-200 sm:flex-row sm:items-center sm:justify-between ${
                  status === "confirmed" ? "bg-white shadow-card" : "bg-surface"
                }`}
              >
                <div className={status === "confirmed" ? "" : "text-muted"}>
                  <div className="flex flex-wrap items-center gap-3">
                    <Link
                      to={`/gimnasios/${booking.gymId}`}
                      className={`font-display text-2xl leading-tight font-semibold underline-offset-4 hover:underline ${status === "confirmed" ? "text-ink" : ""}`}
                    >
                      {booking.gymName}
                    </Link>
                    <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${STATUS_STYLES[status]}`}>
                      {STATUS_LABELS[status]}
                    </span>
                  </div>
                  {booking.gymComuna && <p className="mt-1 text-sm text-muted">{booking.gymComuna}</p>}
                  <p className="mt-2 text-sm tabular-nums first-letter:uppercase">
                    {formatLongDate(booking.date)} · {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
                  </p>
                  {cancelErrors[booking.id] && (
                    <p className="enter mt-2 text-sm text-accent-deep" role="alert">
                      {cancelErrors[booking.id]}
                    </p>
                  )}
                </div>

                {booking.cancellable && !confirming && (
                  <button
                    type="button"
                    onClick={() => setConfirmingId(booking.id)}
                    className="press flex h-10 shrink-0 cursor-pointer items-center justify-center self-start rounded-lg border border-line-strong px-5 text-sm font-semibold hover:border-ink hover:bg-surface sm:self-auto"
                  >
                    Cancelar
                  </button>
                )}

                {booking.cancellable && confirming && (
                  <div className="enter-x flex shrink-0 flex-wrap items-center gap-3 text-sm">
                    <span className="font-semibold">¿Confirmar cancelación?</span>
                    <button
                      type="button"
                      onClick={() => handleCancel(booking.id)}
                      disabled={cancelling}
                      className="press flex h-10 min-w-14 cursor-pointer items-center justify-center rounded-lg bg-accent-deep px-4 font-semibold text-white hover:bg-accent-deep/90 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {cancelling ? "Cancelando…" : "Sí"}
                    </button>
                    <button
                      type="button"
                      onClick={() => setConfirmingId(null)}
                      disabled={cancelling}
                      className="press flex h-10 min-w-14 cursor-pointer items-center justify-center rounded-lg border border-line-strong px-4 font-semibold hover:border-ink hover:bg-surface disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      No
                    </button>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}
