import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { OrderService } from '../../../core/services/order.service';
import { SocketService } from '../../../core/services/socket.service';
import { Order } from '../../../core/models/order.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-billing-management',
  standalone: true,
  imports: [LoadingSpinnerComponent, MatButtonModule, MatIconModule, CurrencyFormatPipe],
  templateUrl: './billing-management.component.html',
})
export class BillingManagementComponent implements OnInit {
  private orderService = inject(OrderService);
  private socket = inject(SocketService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  readonly isLoading = signal(true);
  readonly orders = signal<Order[]>([]);

  ngOnInit(): void {
    this.load();

    this.socket
      .subscribe<unknown>('/topic/admin')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.load());
  }

  private load(): void {
    this.isLoading.set(true);
    this.orderService.getReadyForBillOrders().subscribe({
      next: orders => this.orders.set(orders),
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  estimatedTotal(order: Order): number {
    return order.items.reduce((sum, i) => sum + i.quantity * i.unitPrice, 0);
  }

  openBill(order: Order): void {
    this.router.navigate(['/bill', order.id]);
  }
}
