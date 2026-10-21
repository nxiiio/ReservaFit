import { useMsal } from "@azure/msal-react";
import { useCallback, useEffect, useState } from "react";
import { fetchNotifications, markAllNotificationsRead, markNotificationRead } from "../../lib/api";
import { acquireApiToken } from "../../lib/auth-token";
import { onNotificationsRefresh } from "../../lib/notification-events";
import type { AppNotification } from "../../types/notification";

const POLL_INTERVAL_MS = 30_000;
// A booking reaches ms-notificaciones through RabbitMQ a moment after the booking response,
// so a refresh asked right after booking waits a little before reloading
const REFRESH_DELAY_MS = 1_500;

// "recién", "hace 5 min", "hace 2 h", "ayer", "hace 3 días"
function timeAgo(isoInstant: string): string {
  const minutes = Math.floor((Date.now() - new Date(isoInstant).getTime()) / 60_000);
  if (minutes < 1) return "recién";
  if (minutes < 60) return `hace ${minutes} min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `hace ${hours} h`;
  const days = Math.floor(hours / 24);
  return days === 1 ? "ayer" : `hace ${days} días`;
}

export function NotificationBell() {
  const { instance, accounts } = useMsal();
  const account = accounts[0];
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [open, setOpen] = useState(false);

  const unread = notifications.filter((notification) => !notification.read).length;

  const load = useCallback(async () => {
    // No point polling while the tab is hidden; it reloads when it becomes visible
    if (!account || document.visibilityState === "hidden") return;
    try {
      const token = await acquireApiToken(instance, account);
      if (!token) return;
      setNotifications(await fetchNotifications(token));
    } catch (err) {
      // Not worth bothering the user: the next poll tries again
      console.error(err);
    }
  }, [instance, account]);

  useEffect(() => {
    load();
    const interval = window.setInterval(load, POLL_INTERVAL_MS);

    function handleVisibility() {
      if (document.visibilityState === "visible") load();
    }
    document.addEventListener("visibilitychange", handleVisibility);

    // After booking or canceling (see requestNotificationsRefresh)
    let refreshTimeout: number | undefined;
    const stopListening = onNotificationsRefresh(() => {
      window.clearTimeout(refreshTimeout);
      refreshTimeout = window.setTimeout(load, REFRESH_DELAY_MS);
    });

    return () => {
      window.clearInterval(interval);
      window.clearTimeout(refreshTimeout);
      document.removeEventListener("visibilitychange", handleVisibility);
      stopListening();
    };
  }, [load]);

  async function markRead(notification: AppNotification) {
    if (notification.read || !account) return;
    // Shown as read right away; the next poll corrects it if the request failed
    setNotifications((current) => current.map((n) => (n.id === notification.id ? { ...n, read: true } : n)));
    try {
      const token = await acquireApiToken(instance, account);
      if (token) await markNotificationRead(token, notification.id);
    } catch (err) {
      console.error(err);
    }
  }

  async function markAllRead() {
    if (!account) return;
    setNotifications((current) => current.map((n) => ({ ...n, read: true })));
    try {
      const token = await acquireApiToken(instance, account);
      if (token) await markAllNotificationsRead(token);
    } catch (err) {
      console.error(err);
    }
  }

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        aria-label={unread > 0 ? `Notificaciones, ${unread} sin leer` : "Notificaciones"}
        aria-expanded={open}
        className="relative flex size-10 cursor-pointer items-center justify-center rounded-full border border-line bg-line/60 text-ink transition-colors hover:bg-line"
      >
        <svg aria-hidden="true" viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="1.8">
          <path d="M6 9a6 6 0 1 1 12 0c0 5 2 6.5 2 6.5H4S6 14 6 9Z" strokeLinejoin="round" />
          <path d="M10 19a2 2 0 0 0 4 0" strokeLinecap="round" />
        </svg>
        {unread > 0 && (
          <span className="absolute -top-1 -right-1 flex min-w-5 items-center justify-center rounded-full bg-accent px-1 text-xs font-semibold text-white">
            {unread > 9 ? "9+" : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-10 mt-2 w-80 max-w-[calc(100vw-2.5rem)] rounded-lg border border-line bg-white text-sm shadow-lg">
          <div className="flex items-center justify-between gap-3 border-b border-line px-4 py-3">
            <p className="font-semibold text-ink">Notificaciones</p>
            {unread > 0 && (
              <button
                type="button"
                onClick={markAllRead}
                className="cursor-pointer text-xs font-semibold text-muted hover:text-ink"
              >
                Marcar todas como leídas
              </button>
            )}
          </div>

          {notifications.length === 0 ? (
            <p className="px-4 py-6 text-center text-muted">No tienes notificaciones</p>
          ) : (
            <ul className="max-h-96 overflow-y-auto">
              {notifications.map((notification) => (
                <li key={notification.id} className="border-b border-line last:border-0">
                  <button
                    type="button"
                    onClick={() => markRead(notification)}
                    className={`flex w-full cursor-pointer gap-3 px-4 py-3 text-left hover:bg-paper ${notification.read ? "" : "bg-paper/70"}`}
                  >
                    <span
                      aria-hidden="true"
                      className={`mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full text-xs font-bold ${
                        notification.type === "RESERVA_CANCELADA" ? "bg-line text-muted" : "bg-accent/15 text-accent"
                      }`}
                    >
                      {notification.type === "RESERVA_CANCELADA" ? "✕" : "✓"}
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className={`block ${notification.read ? "text-muted" : "font-medium text-ink"}`}>
                        {notification.message}
                      </span>
                      <span className="mt-1 block text-xs text-muted">{timeAgo(notification.createdAt)}</span>
                    </span>
                    {!notification.read && (
                      <span aria-label="Sin leer" className="mt-2 size-2 shrink-0 rounded-full bg-accent" />
                    )}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
