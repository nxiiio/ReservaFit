import axios from "axios";
import type { Booking, CreateBookingBody } from "../types/booking";
import type { Gym, SlotAvailability } from "../types/gym";
import type { UserProfile } from "../types/user";
import type { ProfileData } from "./profile-schema";

export const API_BASE_URL = "http://localhost:8080";

const api = axios.create({ baseURL: API_BASE_URL });

function authHeader(token: string) {
  return { headers: { Authorization: `Bearer ${token}` } };
}

export async function registerCurrentUser(token: string): Promise<UserProfile> {
  const { data } = await api.post<UserProfile>("/api/usuarios/me", null, authHeader(token));
  return data;
}

export async function completeProfile(token: string, profile: ProfileData): Promise<UserProfile> {
  const { data } = await api.put<UserProfile>("/api/usuarios/me/perfil", profile, authHeader(token));
  return data;
}

// Public endpoint: no token needed
export async function fetchGyms(): Promise<Gym[]> {
  const { data } = await api.get<Gym[]>("/api/gimnasios");
  return data;
}

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
  const { data } = await api.post<Booking>("/api/reservas", body, authHeader(token));
  return data;
}

export async function fetchMyBookings(token: string): Promise<Booking[]> {
  const { data } = await api.get<Booking[]>("/api/reservas/mias", authHeader(token));
  return data;
}

// Soft cancel: returns the booking with status CANCELADA
export async function cancelBooking(token: string, id: number): Promise<Booking> {
  const { data } = await api.delete<Booking>(`/api/reservas/${id}`, authHeader(token));
  return data;
}
