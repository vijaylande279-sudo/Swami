# Hotel Order Management — Frontend

Mobile-first Angular app for waiters, kitchen staff, and admins. Built per
[FRONTEND_GUIDE.md](./FRONTEND_GUIDE.md); read that file before making changes.

## Stack notes

- **State management:** Angular Signals (not NgRx) — the cart and every feature
  component use `signal`/`computed` consistently. See `src/app/store/cart/cart.store.ts`.
- **Styling:** Tailwind CSS v3 for layout/spacing, Angular Material (M3 theming via
  `mat.theme()`, orange primary palette) for interactive components.
- **Auth:** JWT kept in memory only inside `AuthService` — never `localStorage`. A page
  refresh logs the user out by design.
- **Real-time:** `@stomp/stompjs` + `sockjs-client`, wired through `SocketService`.
  `sockjs-client` is dynamically imported inside `connect()` so it never loads during SSR
  or before the user is authenticated.
- **Rendering:** SSR scaffolding is present (Angular CLI default) but every route renders
  `RenderMode.Client` — this is an authenticated, real-time ops tool with nothing to
  prerender or index. See `src/app/app.routes.server.ts`.

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.2.12.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
