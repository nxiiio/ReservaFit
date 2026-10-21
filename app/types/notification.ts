export type NotificationType = "RESERVA_CREADA" | "RESERVA_CANCELADA";

// Named AppNotification so it does not clash with the browser's own Notification type
export type AppNotification = {
  id: number;
  bookingId: number;
  type: NotificationType;
  message: string;
  read: boolean;
  createdAt: string; // ISO instant in UTC, e.g. "2026-10-21T18:30:00Z"
};
