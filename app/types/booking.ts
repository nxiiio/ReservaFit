export type BookingStatus = "CONFIRMADA" | "CANCELADA";

export type Booking = {
  id: number;
  gymId: number;
  gymName: string;
  gymComuna: string | null;
  gymImageUrl: string | null;
  scheduleId: number;
  startTime: string;
  endTime: string;
  date: string; // "YYYY-MM-DD"
  status: BookingStatus;
  cancellable: boolean;
  createdAt: string | null;
};

export type CreateBookingBody = {
  scheduleId: number;
  date: string;
};
