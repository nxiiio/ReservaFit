import {
  BrowserAuthError,
  BrowserAuthErrorCodes,
  InteractionRequiredAuthError,
  type AccountInfo,
  type IPublicClientApplication,
} from "@azure/msal-browser";
import { apiRequest } from "../authConfig";

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
