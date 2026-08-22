import { Component, effect, inject } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { SocketService } from '../../../core/services/socket.service';
import { ToastService } from '../../../core/services/toast.service';
import { AdminNotification } from '../../../core/models/admin-notification.model';

@Component({
  selector: 'app-admin-notifier',
  standalone: true,
  template: '',
})
export class AdminNotifierComponent {
  private auth = inject(AuthService);
  private socket = inject(SocketService);
  private toast = inject(ToastService);

  constructor() {
    // Mounted once at the app root, so the socket isn't connected yet on init — resubscribe
    // whenever `connected` flips true (initial connect, or after a drop/reconnect).
    effect(onCleanup => {
      if (!this.socket.connected() || this.auth.user()?.role !== 'ADMIN') return;

      const sub = this.socket.subscribe<AdminNotification>('/topic/admin').subscribe(notification => {
        if (notification.status === 'PAID') {
          this.toast.success(notification.message);
        } else {
          this.toast.info(notification.message);
        }
      });
      onCleanup(() => sub.unsubscribe());
    });
  }
}
