import { Component, DestroyRef, OnInit, PLATFORM_ID, inject, signal } from '@angular/core';
import { DatePipe, isPlatformBrowser } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { OrderService } from '../../../core/services/order.service';
import { SocketService } from '../../../core/services/socket.service';
import { AuthService } from '../../../core/services/auth.service';
import { MenuLookupService } from '../../../core/services/menu-lookup.service';
import { Order, OrderItem } from '../../../core/models/order.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-kitchen-queue',
  standalone: true,
  imports: [LoadingSpinnerComponent, DatePipe, MatIconModule],
  templateUrl: './kitchen-queue.component.html',
})
export class KitchenQueueComponent implements OnInit {
  private orderService = inject(OrderService);
  private socket = inject(SocketService);
  private auth = inject(AuthService);
  private menuLookup = inject(MenuLookupService);
  private platformId = inject(PLATFORM_ID);
  private destroyRef = inject(DestroyRef);

  readonly orders = signal<Order[]>([]);
  readonly isLoading = signal(true);
  readonly newOrderAlert = signal<Order | null>(null);

  private alertTimeout?: ReturnType<typeof setTimeout>;

  ngOnInit(): void {
    this.menuLookup.ensureLoaded();
    this.loadQueue();

    this.socket
      .subscribe<Order>('/topic/kitchen')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(order => {
        const isNew = !this.orders().some(o => o.id === order.id);
        this.upsertOrder(order);
        if (isNew) this.announceNewOrder(order);
      });
  }

  private loadQueue(): void {
    this.isLoading.set(true);
    this.orderService.getKitchenQueue().subscribe({
      next: orders => this.orders.set(orders),
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  private upsertOrder(order: Order): void {
    this.orders.update(orders => {
      const existing = orders.find(o => o.id === order.id);
      if (!existing) return [order, ...orders];
      // Some broadcasts on this topic omit the items array (status-only pushes) — never
      // let that wipe out an items list we already have for this order.
      const merged: Order = { ...order, items: order.items?.length ? order.items : existing.items };
      return orders.map(o => (o.id === order.id ? merged : o));
    });
  }

  private announceNewOrder(order: Order): void {
    this.newOrderAlert.set(order);
    this.playAlert();

    clearTimeout(this.alertTimeout);
    this.alertTimeout = setTimeout(() => this.newOrderAlert.set(null), 8000);
  }

  dismissAlert(): void {
    clearTimeout(this.alertTimeout);
    this.newOrderAlert.set(null);
  }

  private playAlert(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    const audio = new Audio('/sounds/new-order.mp3');
    audio.play().catch(() => {});
  }

  startPreparing(order: Order): void {
    this.orderService.markPreparing(order.id).subscribe({
      next: updatedOrder => this.upsertOrder(updatedOrder),
      error: () => {},
    });
  }

  markReady(order: Order): void {
    this.orderService.markReady(order.id).subscribe({
      next: updatedOrder => this.upsertOrder(updatedOrder),
      error: () => {},
    });
  }

  logout(): void {
    this.socket.disconnect();
    this.auth.logout();
  }

  itemName(item: OrderItem): string {
    return item.name || this.menuLookup.nameFor(item.menuItemId) || `Item #${item.menuItemId}`;
  }

  // Backend may signal "kitchen finished" as either 'READY' or 'ORDER_READY' — accept
  // either until the order-level contract is confirmed (table.status confirmed 'ORDER_READY').
  isReady(order: Order): boolean {
    return order.status === 'READY' || order.status === 'ORDER_READY';
  }
}
