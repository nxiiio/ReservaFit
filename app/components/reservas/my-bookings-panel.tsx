import { useMsal } from "@azure/msal-react";
import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router";
import { cancelBooking, fetchMyBookings } from "../../lib/api";
import { getErrorMessage } from "../../lib/api-errors";
import { acquireApiToken } from "../../lib/auth-token";
import type { Booking } from "../../types/booking";
import { formatLongDate, formatTime, todayIso } from "../../lib/dates";

type BookingsState = { status: "loading" } | { status: "error" } | { status: "ready"; bookings: Booking[] };

type DisplayStatus = "confirmed" | "cancelled" | "finished";

const STATUS_LABELS: Record<DisplayStatus, string> = {
  confirmed: "Confirmada",
  cancelled: "Cancelada",
  finished: "Finalizada",
};

const STATUS_STYLES: Record<DisplayStatus, string> = {
  confirmed: "bg-ink text-white",
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
      <h1 className="font-display text-3xl font-bold sm:text-4xl">Mis reservas</h1>

      {justBooked && (
        <p className="mt-6 rounded-md border border-line bg-white px-4 py-3 text-sm font-medium" role="status">
          ¡Reserva confirmada! Te esperamos.
        </p>
      )}

      {state.status === "loading" && <p className="mt-8 text-sm text-muted">Cargando reservas…</p>}

      {state.status === "error" && (
        <p className="mt-8 text-sm text-[#b94018]">
          No se pudieron cargar tus reservas.{" "}
          <button type="button" onClick={() => setAttempt((n) => n + 1)} className="cursor-pointer font-semibold underline">
            Reintentar
          </button>
        </p>
      )}

      {state.status === "ready" && state.bookings.length === 0 && (
        <div className="mt-8 rounded-lg border border-line bg-white p-6 text-sm">
          <p className="font-semibold">Aún no tienes reservas.</p>
          <p className="mt-1 text-muted">Elige un gimnasio y reserva tu próximo entrenamiento.</p>
          <Link
            to="/reservas"
            className="mt-5 inline-flex h-10 items-center justify-center rounded-md bg-ink px-5 font-semibold text-white transition-colors hover:bg-ink/85"
          >
            Ver gimnasios
          </Link>
        </div>
      )}

      {state.status === "ready" && state.bookings.length > 0 && (
        <ul className="mt-8 flex flex-col gap-4">
          {state.bookings.map((booking) => {
            const status = displayStatus(booking, today);
            const confirming = confirmingId === booking.id;
            const cancelling = cancellingId === booking.id;
            return (
              <li
                key={booking.id}
                className="flex flex-col gap-4 rounded-lg border border-line bg-white p-4 sm:flex-row sm:items-center sm:justify-between"
              >
                <div className={status === "confirmed" ? "" : "text-muted"}>
                  <div className="flex flex-wrap items-center gap-3">
                    <Link
                      to={`/gimnasios/${booking.gymId}`}
                      className={`font-display text-xl font-semibold hover:underline ${status === "confirmed" ? "text-ink" : ""}`}
                    >
                      {booking.gymName}
                    </Link>
                    <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${STATUS_STYLES[status]}`}>
                      {STATUS_LABELS[status]}
                    </span>
                  </div>
                  {booking.gymComuna && <p className="mt-1 text-sm text-muted">{booking.gymComuna}</p>}
                  <p className="mt-2 text-sm first-letter:uppercase">
                    {formatLongDate(booking.date)} · {formatTime(booking.startTime)} – {formatTime(booking.endTime)}
                  </p>
                  {cancelErrors[booking.id] && (
                    <p className="mt-2 text-sm text-[#b94018]" role="alert">
                      {cancelErrors[booking.id]}
                    </p>
                  )}
                </div>

                {booking.cancellable && !confirming && (
                  <button
                    type="button"
                    onClick={() => setConfirmingId(booking.id)}
                    className="flex h-10 shrink-0 cursor-pointer items-center justify-center rounded-md border border-line px-5 text-sm font-semibold transition-colors hover:bg-paper"
                  >
                    Cancelar
                  </button>
                )}

                {booking.cancellable && confirming && (
                  <div className="flex shrink-0 flex-wrap items-center gap-3 text-sm">
                    <span className="font-semibold">¿Confirmar cancelación?</span>
                    <button
                      type="button"
                      onClick={() => handleCancel(booking.id)}
                      disabled={cancelling}
                      className="flex h-10 cursor-pointer items-center justify-center rounded-md bg-ink px-4 font-semibold text-white transition-colors hover:bg-ink/85 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {cancelling ? "Cancelando…" : "Sí"}
                    </button>
                    <button
                      type="button"
                      onClick={() => setConfirmingId(null)}
                      disabled={cancelling}
                      className="flex h-10 cursor-pointer items-center justify-center rounded-md border border-line px-4 font-semibold transition-colors hover:bg-paper disabled:cursor-not-allowed disabled:opacity-50"
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
