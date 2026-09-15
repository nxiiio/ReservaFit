import { type RouteConfig, index, layout, route } from "@react-router/dev/routes";

export default [
  layout("routes/layout.tsx", [
    index("routes/home.tsx"),
    route("login", "routes/login.tsx"),
    // Signed-in pages: guards + app nav
    layout("routes/app-layout.tsx", [
      route("reservas", "routes/reservas.tsx"),
      route("gimnasios/:id", "routes/gimnasio.tsx"),
      route("mis-reservas", "routes/mis-reservas.tsx"),
    ]),
  ]),
] satisfies RouteConfig;
