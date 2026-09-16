import axios from "axios";

// Field -> message map from a 400/409 body; null for single-message ({"error": "..."}) bodies
export function getFieldErrors(err: unknown): Record<string, string> | null {
  if (!axios.isAxiosError(err)) return null;
  const data = err.response?.data;
  if (!data || typeof data !== "object" || "error" in data || Object.keys(data).length === 0) {
    return null;
  }
  return data as Record<string, string>;
}

// The backend's single-message error body ({"error": "..."}), or the first field message
export function getErrorMessage(err: unknown): string | null {
  if (!axios.isAxiosError(err)) return null;
  const data = err.response?.data;
  if (!data || typeof data !== "object") return null;
  const values = Object.values(data as Record<string, unknown>).filter((v): v is string => typeof v === "string");
  const error = (data as Record<string, unknown>).error;
  return typeof error === "string" ? error : (values[0] ?? null);
}

export function getErrorStatus(err: unknown): number | null {
  return axios.isAxiosError(err) ? (err.response?.status ?? null) : null;
}
