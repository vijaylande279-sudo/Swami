import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// sockjs-client (loaded dynamically by SocketService, browser-only) expects Node's
// `global` object, which doesn't exist in real browsers — polyfill it before anything
// can import that package.
(window as unknown as { global: Window }).global = window;

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
