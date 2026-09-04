import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

const CHECKOUT_JS_SRC = 'https://checkout.razorpay.com/v1/checkout.js';

/**
 * Loads Razorpay's hosted checkout script as a plain <script> tag, never via npm -
 * per doc §15.1, the checkout UI itself is Razorpay's iframe; we never build our own
 * card form or bundle their SDK into our build.
 */
@Injectable({ providedIn: 'root' })
export class RazorpayLoaderService {
  private loaded = false;

  ensureLoaded(): Observable<void> {
    return new Observable(subscriber => {
      if (this.loaded || window.Razorpay) {
        this.loaded = true;
        subscriber.next();
        subscriber.complete();
        return;
      }

      const existing = document.querySelector(`script[src="${CHECKOUT_JS_SRC}"]`);
      if (existing) {
        existing.addEventListener('load', () => {
          this.loaded = true;
          subscriber.next();
          subscriber.complete();
        });
        return;
      }

      const script = document.createElement('script');
      script.src = CHECKOUT_JS_SRC;
      script.onload = () => {
        this.loaded = true;
        subscriber.next();
        subscriber.complete();
      };
      script.onerror = () => subscriber.error(new Error('Failed to load Razorpay checkout script'));
      document.body.appendChild(script);
    });
  }
}
