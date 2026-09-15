import axios from "axios";
import {
  BrowserAuthError,
  BrowserAuthErrorCodes,
  InteractionRequiredAuthError,
  type AccountInfo,
  type IPublicClientApplication,
} from "@azure/msal-browser";
import { apiRequest } from "../authConfig";
import type { ProfileData } from "./profile-schema";

export const API_BASE_URL = "http://localhost:8080";

export type UserProfile = {
  id: number;
  name: string | null;
  email: string | null;
  profileComplete: boolean;
  registeredAt: string | null;
};

export type Gym = {
  id: number;
  name: string;
  comuna: string | null;
  address: string | null;
  description: string | null;
  imageUrl: string | null;
};

const api = axios.create({ baseURL: API_BASE_URL });

// Returns null when an interactive redirect was started: the page is about to navigate away.
export async function acquireApiToken(
  instance: IPublicClientApplication,
  account: AccountInfo,
): Promise<string | null> {
  try {
    const result = await instance.acquireTokenSilent({ ...apiRequest, account });
    return result.accessToken;
  } catch (err) {
    // timed_out happens with personal accounts when the hidden silent-auth iframe is blocked.
    const needsInteraction =
      err instanceof InteractionRequiredAuthError ||
      (err instanceof BrowserAuthError && err.errorCode === BrowserAuthErrorCodes.timedOut);
    if (!needsInteraction) throw err;
    await instance.acquireTokenRedirect(apiRequest);
    return null;
  }
}

export function getFieldErrors(err: unknown): Record<string, string> | null {
  if (!axios.isAxiosError(err)) return null;
  const data = err.response?.data;
  if (!data || typeof data !== "object" || "error" in data || Object.keys(data).length === 0) {
    return null;
  }
  return data as Record<string, string>;
}

export async function registerCurrentUser(token: string): Promise<UserProfile> {
  const { data } = await api.post<UserProfile>("/api/usuarios/me", null, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
}

// Public endpoint: no token needed
export async function fetchGyms(): Promise<Gym[]> {
  const { data } = await api.get<Gym[]>("/api/gimnasios");
  return data;
}

export type SlotAvailability = {
  id: number;
  startTime: string;
  endTime: string;
  available: boolean;
};

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

// Public endpoint: no token needed
export async function fetchGym(id: number | string): Promise<Gym> {
  const { data } = await api.get<Gym>(`/api/gimnasios/${id}`);
  return data;
}

// Public endpoint: no token needed
export async function fetchAvailability(gymId: number | string, date: string): Promise<SlotAvailability[]> {
  const { data } = await api.get<SlotAvailability[]>(`/api/gimnasios/${gymId}/disponibilidad`, {
    params: { fecha: date },
  });
  return data;
}

export async function createBooking(token: string, body: CreateBookingBody): Promise<Booking> {
  const { data } = await api.post<Booking>("/api/reservas", body, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
}

export async function fetchMyBookings(token: string): Promise<Booking[]> {
  const { data } = await api.get<Booking[]>("/api/reservas/mias", {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
}

// Soft cancel: returns the booking with status CANCELADA
export async function cancelBooking(token: string, id: number): Promise<Booking> {
  const { data } = await api.delete<Booking>(`/api/reservas/${id}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
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

export async function completeProfile(token: string, profile: ProfileData): Promise<UserProfile> {
  const { data } = await api.put<UserProfile>("/api/usuarios/me/perfil", profile, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
}
