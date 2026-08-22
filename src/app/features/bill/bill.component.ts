import { Component, DestroyRef, OnInit, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatIconModule } from '@angular/material/icon';
import { OrderService } from '../../core/services/order.service';
import { BillService } from '../../core/services/bill.service';
import { SocketService } from '../../core/services/socket.service';
import { AuthService } from '../../core/services/auth.service';
import { MenuLookupService } from '../../core/services/menu-lookup.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { Order } from '../../core/models/order.model';
import { Bill, BillItem, PaymentMode } from '../../core/models/bill.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyFormatPipe } from '../../shared/pipes/currency-format.pipe';
import { PrintableBillComponent } from './printable-bill/printable-bill.component';

@Component({
  selector: 'app-bill',
  standalone: true,
  imports: [
    LoadingSpinnerComponent,
    CurrencyFormatPipe,
    MatButtonToggleModule,
    MatIconModule,
    PrintableBillComponent,
  ],
  templateUrl: './bill.component.html',
})
export class BillComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  private billService = inject(BillService);
  private socket = inject(SocketService);
  private auth = inject(AuthService);
  private menuLookup = inject(MenuLookupService);
  private toast = inject(ToastService);
  private confirmDialog = inject(ConfirmDialogService);
  private platformId = inject(PLATFORM_ID);
  private destroyRef = inject(DestroyRef);

  readonly isLoading = signal(true);
  readonly isProcessing = signal(false);
  readonly order = signal<Order | null>(null);
  readonly bill = signal<Bill | null>(null);
  readonly paymentMode = signal<PaymentMode>('CASH');

  private orderId = 0;

  // Backend may signal "kitchen finished" as either 'READY' or 'ORDER_READY' (the latter
  // is confirmed for table.status) — accept either until the order-level contract is confirmed.
  readonly isReadyToServe = computed(() => {
    const status = this.order()?.status;
    return status === 'READY' || status === 'ORDER_READY';
  });
  readonly isAdmin = computed(() => this.auth.user()?.role === 'ADMIN');

  ngOnInit(): void {
    this.menuLookup.ensureLoaded();
    this.orderId = Number(this.route.snapshot.paramMap.get('orderId'));
    this.loadOrder();

    this.socket
      .subscribe<Order>('/topic/kitchen')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(updatedOrder => {
        if (updatedOrder.id !== this.orderId) return;
        // Some broadcasts on this topic omit the items array (status-only pushes) — never
        // let that wipe out an items list we already loaded via the initial REST fetch.
        this.order.update(current => ({
          ...updatedOrder,
          items: updatedOrder.items?.length ? updatedOrder.items : (current?.items ?? updatedOrder.items),
        }));
      });
  }

  private loadOrder(): void {
    this.isLoading.set(true);
    this.orderService.getOrder(this.orderId).subscribe({
      next: order => {
        this.order.set(order);
        if (order.status === 'BILLED' || order.status === 'CLOSED') {
          this.loadBill();
        } else {
          this.isLoading.set(false);
        }
      },
      error: () => this.isLoading.set(false),
    });
  }

  private loadBill(): void {
    this.billService.getBill(this.orderId).subscribe({
      next: bill => {
        this.bill.set(bill);
        this.paymentMode.set(bill.paymentMode ?? 'CASH');
      },
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  async markReadyForBill(): Promise<void> {
    const confirmed = await this.confirmDialog.open('Mark this table ready for billing? This notifies the admin.');
    if (!confirmed) return;

    this.isProcessing.set(true);
    this.orderService.markServed(this.orderId).subscribe({
      next: () => {
        this.orderService.notifyReadyForBill(this.orderId).subscribe({
          next: order => {
            this.order.set(order);
            this.toast.success('Admin notified — ready for bill.');
          },
          error: () => this.isProcessing.set(false),
          complete: () => this.isProcessing.set(false),
        });
      },
      error: () => this.isProcessing.set(false),
    });
  }

  generateBill(): void {
    this.isProcessing.set(true);
    this.billService.generateBill(this.orderId).subscribe({
      next: bill => {
        this.bill.set(bill);
        this.paymentMode.set(bill.paymentMode ?? 'CASH');
        this.order.update(o => (o ? { ...o, status: 'BILLED' } : o));
      },
      error: () => this.isProcessing.set(false),
      complete: () => this.isProcessing.set(false),
    });
  }

  async markPaid(): Promise<void> {
    const bill = this.bill();
    if (!bill) return;

    const confirmed = await this.confirmDialog.open(`Mark this bill as paid via ${this.paymentMode()}?`);
    if (!confirmed) return;

    this.isProcessing.set(true);
    this.billService.markPaid(bill.id, this.paymentMode()).subscribe({
      next: updated => {
        this.bill.set(updated);
        this.toast.success('Payment recorded. Table is now available.');
        this.router.navigate(['/tables']);
      },
      error: () => this.isProcessing.set(false),
      complete: () => this.isProcessing.set(false),
    });
  }

  printBill(): void {
    if (isPlatformBrowser(this.platformId)) window.print();
  }

  backToTables(): void {
    this.router.navigate(['/tables']);
  }

  itemName(item: { name: string } & Pick<BillItem, 'menuItemId'>): string {
    return item.name || this.menuLookup.nameFor(item.menuItemId) || `Item #${item.menuItemId}`;
  }
}
