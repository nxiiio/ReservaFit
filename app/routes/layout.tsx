import { Outlet } from "react-router";
import { Footer } from "../components/layout/footer";

export default function SiteLayout() {
  return (
    <div className="flex min-h-dvh flex-col">
      <div className="flex flex-1 flex-col">
        <Outlet />
      </div>
      <Footer />
    </div>
  );
}
