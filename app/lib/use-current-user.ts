import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";
import { acquireApiToken, registerCurrentUser, type UserProfile } from "./api";

export type CurrentUserState =
  | { status: "loading" }
  | { status: "ready"; profile: UserProfile }
  | { status: "error" };

// Registers the signed-in user on the backend (idempotent) and returns their profile.
export function useCurrentUser(enabled: boolean) {
  const { instance, accounts } = useMsal();
  const account = accounts[0];
  const [state, setState] = useState<CurrentUserState>({ status: "loading" });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    if (!enabled || !account) return;

    let cancelled = false;
    setState({ status: "loading" });

    (async () => {
      try {
        const token = await acquireApiToken(instance, account);
        if (!token) return;
        const profile = await registerCurrentUser(token);
        if (!cancelled) setState({ status: "ready", profile });
      } catch (err) {
        console.error(err);
        if (!cancelled) setState({ status: "error" });
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [enabled, account, instance, attempt]);

  return { state, retry: () => setAttempt((n) => n + 1) };
}
