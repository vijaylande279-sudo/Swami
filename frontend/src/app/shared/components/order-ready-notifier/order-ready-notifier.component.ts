import { Component, PLATFORM_ID, effect, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { SocketService } from '../../../core/services/socket.service';
import { ToastService } from '../../../core/services/toast.service';
import { OrderReadyNotification } from '../../../core/models/order.model';

@Component({
  selector: 'app-order-ready-notifier',
  standalone: true,
  template: '',
})
export class OrderReadyNotifierComponent {
  private socket = inject(SocketService);
  private toast = inject(ToastService);
  private platformId = inject(PLATFORM_ID);

  constructor() {
    // Mounted once at the app root, so the socket isn't connected yet on init — resubscribe
    // whenever `connected` flips true (initial connect, or after a drop/reconnect).
    effect(onCleanup => {
      if (!this.socket.connected()) return;

      const sub = this.socket.subscribe<OrderReadyNotification>('/topic/waiter').subscribe(notification => {
        this.toast.success(`Table ${notification.tableNumber} — order ready, please serve!`);
        this.playAlert();
      });
      onCleanup(() => sub.unsubscribe());
    });
  }

  private playAlert(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const audio = new Audio('/sounds/order-ready.mp3');
    audio.play().catch(() => {});
  }
}
