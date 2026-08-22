import { RenderMode, ServerRoute } from '@angular/ssr';

// This app is an authenticated, real-time ops tool (waiter/kitchen tablets) — there is
// nothing to prerender or SEO-index, so every route renders client-side only.
export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
