import { InteractionStatus } from "@azure/msal-browser";
import { useIsAuthenticated, useMsal } from "@azure/msal-react";
import { Navigate, Outlet } from "react-router";
import { AppNav } from "../components/reservas/app-nav";
import { useCurrentUser } from "../lib/use-current-user";

// Shared shell for signed-in pages: auth guard, profile-complete guard and the app nav
export default function AppLayout() {
  const isAuthenticated = useIsAuthenticated();
  const { accounts, inProgress } = useMsal();
  const { state } = useCurrentUser(Boolean(accounts[0]));

  if (!isAuthenticated && inProgress === InteractionStatus.None) {
    return <Navigate to="/login" replace />;
  }

  if (state.status === "ready" && !state.profile.profileComplete) {
    return <Navigate to="/login" replace />;
  }

  const userName = state.status === "ready" ? state.profile.name : null;

  return (
    <>
      <AppNav userName={userName} />
      <Outlet />
    </>
  );
}
