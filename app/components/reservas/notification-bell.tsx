import { useMsal } from "@azure/msal-react";
import { useCallback, useEffect, useState } from "react";
import { fetchNotifications, markAllNotificationsRead, markNotificationRead } from "../../lib/api";
import { acquireApiToken } from "../../lib/auth-token";
import { onNotificationsRefresh } from "../../lib/notification-events";
import type { AppNotification } from "../../types/notification";
import { BellIcon, CheckIcon, CloseIcon } from "../ui/icons";
import { StateBlock } from "../ui/state-block";

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
    // Not relative on phones: the panel then anchors to AppNav's button cluster, whose right
    // edge sits on the page gutter, so the 20rem panel stays on screen at 375px
    <div className="sm:relative">
      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        aria-label={unread > 0 ? `Notificaciones, ${unread} sin leer` : "Notificaciones"}
        aria-expanded={open}
        className="press relative flex size-10 cursor-pointer items-center justify-center rounded-full border border-line bg-surface text-ink hover:border-line-strong hover:bg-line aria-expanded:border-ink"
      >
        <BellIcon />
        {unread > 0 && (
          // Ink on accent is 5.5:1; the paper ring separates the pill from the button edge
          <span
            aria-hidden="true"
            className="pop absolute -top-1.5 -right-1.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-accent px-1 text-[11px] leading-none font-bold text-ink tabular-nums ring-2 ring-paper"
          >
            {unread > 9 ? "9+" : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="pop absolute right-0 z-10 mt-2 w-80 max-w-[calc(100vw-2.5rem)] overflow-hidden rounded-lg bg-white text-sm shadow-pop">
          <div className="flex items-center justify-between gap-3 border-b border-line py-2 pr-2 pl-4">
            <p className="py-1.5 font-semibold text-ink">Notificaciones</p>
            {unread > 0 && (
              <button
                type="button"
                onClick={markAllRead}
                className="press cursor-pointer rounded-md px-2 py-1.5 text-xs font-semibold text-muted hover:bg-surface hover:text-ink"
              >
                Marcar todas como leídas
              </button>
            )}
          </div>

          {notifications.length === 0 ? (
            <div className="p-2">
              <StateBlock icon={<BellIcon />} title="No tienes notificaciones" />
            </div>
          ) : (
            <ul className="max-h-96 overflow-y-auto overscroll-contain">
              {notifications.map((notification) => {
                const cancelled = notification.type === "RESERVA_CANCELADA";
                return (
                  <li key={notification.id} className="border-b border-line last:border-0">
                    <button
                      type="button"
                      onClick={() => markRead(notification)}
                      className={`flex w-full cursor-pointer gap-3 px-4 py-3 text-left transition-colors hover:bg-surface ${
                        notification.read ? "" : "bg-surface/60"
                      }`}
                    >
                      <span
                        aria-hidden="true"
                        className={`mt-0.5 flex size-7 shrink-0 items-center justify-center rounded-full ${
                          cancelled ? "bg-line text-muted" : "bg-accent/15 text-accent-deep"
                        } ${notification.read ? "opacity-70" : ""}`}
                      >
                        {cancelled ? <CloseIcon className="size-3.5" /> : <CheckIcon className="size-3.5" />}
                      </span>
                      <span className="min-w-0 flex-1 leading-5">
                        <span className={`block ${notification.read ? "text-muted" : "font-semibold text-ink"}`}>
                          {notification.message}
                        </span>
                        <span className="mt-1 block text-xs text-muted tabular-nums">{timeAgo(notification.createdAt)}</span>
                      </span>
                      {!notification.read && (
                        <span aria-label="Sin leer" className="mt-2 size-2 shrink-0 rounded-full bg-accent-deep" />
                      )}
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
