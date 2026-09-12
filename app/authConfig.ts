import type { Configuration } from "@azure/msal-browser";
import { LogLevel } from "@azure/msal-browser";

const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;
const redirectUri = import.meta.env.VITE_AZURE_REDIRECT_URI;

if (!clientId || !redirectUri) {
    throw new Error(
        "Faltan variables de entorno de MSAL: revisa VITE_AZURE_CLIENT_ID y VITE_AZURE_REDIRECT_URI en .env",
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
    scopes: ["User.Read"],
};