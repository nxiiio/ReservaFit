import { useEffect, useState } from "react";
import { Outlet } from "react-router";
import { PublicClientApplication, type IPublicClientApplication } from "@azure/msal-browser";
import { MsalProvider } from "@azure/msal-react";
import { Footer } from "../components/layout/footer";
import { msalConfig } from "../lib/msal-config";

export default function SiteLayout() {
  const [msalInstance, setMsalInstance] = useState<IPublicClientApplication | null>(null);
  const [initError, setInitError] = useState(false);

  useEffect(() => {
    const instance = new PublicClientApplication(msalConfig);
    instance
      .initialize()
      .then(() => setMsalInstance(instance))
      .catch(() => setInitError(true));
  }, []);

  if (initError) {
    return (
      <div className="flex min-h-dvh items-center justify-center p-5 text-center text-sm text-muted">
        No se pudo inicializar el inicio de sesión. Recarga la página o intenta más tarde.
      </div>
    );
  }

  if (!msalInstance) return null;

  return (
    <MsalProvider instance={msalInstance}>
      <div className="flex min-h-dvh flex-col">
        <div className="flex flex-1 flex-col">
          <Outlet />
        </div>
        <Footer />
      </div>
    </MsalProvider>
  );
}
