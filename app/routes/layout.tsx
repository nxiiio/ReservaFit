import { useEffect, useState } from "react";
import { Outlet } from "react-router";
import { PublicClientApplication, type IPublicClientApplication } from "@azure/msal-browser";
import { MsalProvider } from "@azure/msal-react";
import { Footer } from "../components/layout/footer";
import { msalConfig } from "../lib/msal-config";

export default function SiteLayout() {
  const [msalInstance, setMsalInstance] = useState<IPublicClientApplication | null>(null);

  useEffect(() => {
    const instance = new PublicClientApplication(msalConfig);
    instance.initialize().then(() => setMsalInstance(instance));
  }, []);

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
