import type { Configuration } from "@azure/msal-browser";
import { LogLevel } from "@azure/msal-browser";

const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;
const redirectUri = import.meta.env.VITE_AZURE_REDIRECT_URI;
const apiScope = import.meta.env.VITE_API_SCOPE;

if (!clientId || !redirectUri || !apiScope) {
    throw new Error(
        "Faltan variables de entorno de MSAL: revisa VITE_AZURE_CLIENT_ID, VITE_AZURE_REDIRECT_URI y VITE_API_SCOPE en .env",
    );
}

export const msalConfig: Configuration = {
    auth: {
        clientId,
        authority: "https://login.microsoftonline.com/consumers",
        redirectUri,
        postLogoutRedirectUri: redirectUri,
    },
    cache: {
        cacheLocation: "sessionStorage",
    },
    system: {
        loggerOptions: {
        loggerCallback: (level, message, containsPii) => {
            if (containsPii) return;
            if (level === LogLevel.Error) console.error(message);
        },
        logLevel: LogLevel.Error,
        },
    },
};

export const loginRequest = {
    // Requesting the API scope at login grants consent up front and caches its token,
    // so acquireTokenSilent doesn't need a hidden iframe (which hangs for personal accounts).
    scopes: [apiScope],
    // Always show Microsoft's account picker instead of silently reusing the
    // browser's existing SSO session — otherwise, since logout only clears
    // ReservaFit's local session (see reservas-panel.tsx), users could never
    // switch to a different Microsoft account.
    prompt: "select_account",
};

// Separate token request for calling ReservaFit-Backend directly — the
// loginRequest token above is scoped to Microsoft Graph and its audience
// won't be accepted by our own API.
export const apiRequest = {
    scopes: [apiScope],
};