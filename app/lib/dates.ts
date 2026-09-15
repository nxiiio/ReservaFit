// Dates travel as "YYYY-MM-DD" strings. Never use toISOString() or new Date("YYYY-MM-DD"):
// both are UTC, which shifts the day in Chile's negative offset.

function pad(n: number): string {
  return String(n).padStart(2, "0");
}

export function toIsoDate(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function parseIsoDate(iso: string): Date {
  const [year, month, day] = iso.split("-").map(Number);
  return new Date(year, month - 1, day);
}

export function todayIso(): string {
  return toIsoDate(new Date());
}

export function addDaysIso(iso: string, days: number): string {
  const date = parseIsoDate(iso);
  date.setDate(date.getDate() + days);
  return toIsoDate(date);
}

// "lunes, 14 de septiembre de 2026"
export function formatLongDate(iso: string): string {
  return parseIsoDate(iso).toLocaleDateString("es-CL", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

// "07:00:00" -> "07:00"
export function formatTime(time: string): string {
  return time.slice(0, 5);
}
