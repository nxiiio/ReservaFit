const REFRESH_EVENT = "notifications:refresh";

// Asks the notification bell to reload now (after booking or canceling) instead of
// waiting for its next poll. A window event, so no props need to be passed around.
export function requestNotificationsRefresh() {
  window.dispatchEvent(new Event(REFRESH_EVENT));
}

export function onNotificationsRefresh(listener: () => void): () => void {
  window.addEventListener(REFRESH_EVENT, listener);
  return () => window.removeEventListener(REFRESH_EVENT, listener);
}
