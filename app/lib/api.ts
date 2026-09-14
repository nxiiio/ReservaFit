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
  const { data } = await api.post<UserProfile>("/api/users/me", null, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
}

export async function completeProfile(token: string, profile: ProfileData): Promise<UserProfile> {
  const { data } = await api.put<UserProfile>("/api/users/me/profile", profile, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return data;
}
