import { InteractionStatus } from "@azure/msal-browser";
import { useMsal } from "@azure/msal-react";
import { useState } from "react";
import { NavLink, useLocation } from "react-router";
import { Brand } from "../landing/brand";

const activeLink = "border-b-2 border-ink py-1 font-semibold";
const idleLink = "py-1 text-muted transition-colors hover:text-ink";

export function AppNav({ userName }: { userName: string | null }) {
  const { instance, accounts, inProgress } = useMsal();
  const { pathname } = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const account = accounts[0];
  // A gym's schedule page belongs to the "Gimnasios" section
  const onGymPage = pathname.startsWith("/gimnasios/");

  function handleLogout() {
    // clearCache() only wipes the local MSAL cache — unlike logoutRedirect(),
    // it never navigates to Microsoft's logout endpoint, so it doesn't end
    // the browser's Microsoft SSO session (Outlook, Teams, etc. stay logged in).
    // It also doesn't fire an MSAL event, so MsalProvider's React state (and
    // therefore the app layout's guard) would never notice the account is gone —
    // a hard navigation forces a fresh read of the (now empty) cache on load.
    instance
      .clearCache({ account })
      .then(() => window.location.assign("/login"))
      .catch(console.error);
  }

  return (
    <header className="border-b border-line">
      <nav
        aria-label="Navegación principal"
        className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-x-4 gap-y-4 px-5 py-5 sm:px-8 lg:px-12"
      >
        <Brand />
        <div className="order-3 flex w-full items-center justify-center gap-6 border-t border-line pt-4 text-sm font-medium sm:order-none sm:w-auto sm:border-0 sm:pt-0">
          <NavLink
            to="/reservas"
            className={({ isActive }) => (isActive || onGymPage ? activeLink : idleLink)}
            aria-current={onGymPage ? "page" : undefined}
          >
            Gimnasios
          </NavLink>
          <NavLink to="/mis-reservas" className={({ isActive }) => (isActive ? activeLink : idleLink)}>
            Mis reservas
          </NavLink>
        </div>
        <div className="relative">
          <button
            type="button"
            onClick={() => setMenuOpen((open) => !open)}
            aria-label="Menú de usuario"
            aria-expanded={menuOpen}
            className="flex size-10 cursor-pointer items-center justify-center rounded-full border border-line bg-line/60 text-ink transition-colors hover:bg-line"
          >
            <svg aria-hidden="true" viewBox="0 0 24 24" className="size-5" fill="none" stroke="currentColor" strokeWidth="1.8">
              <circle cx="12" cy="8" r="4" />
              <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" strokeLinecap="round" />
            </svg>
          </button>
          {menuOpen && (
            <div className="absolute right-0 z-10 mt-2 w-56 rounded-lg border border-line bg-white p-2 text-sm shadow-lg">
              {userName && <p className="truncate px-3 py-2 text-muted">{userName}</p>}
              <button
                type="button"
                onClick={handleLogout}
                disabled={inProgress !== InteractionStatus.None}
                className="w-full cursor-pointer rounded-md px-3 py-2 text-left font-semibold hover:bg-paper disabled:cursor-not-allowed disabled:opacity-50"
              >
                Cerrar sesión
              </button>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
}
