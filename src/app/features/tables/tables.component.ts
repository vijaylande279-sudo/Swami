import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { TableService } from '../../core/services/table.service';
import { SocketService } from '../../core/services/socket.service';
import { ToastService } from '../../core/services/toast.service';
import { CartStore } from '../../store/cart/cart.store';
import { Table } from '../../core/models/table.model';
import { TableCardComponent } from './table-card/table-card.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { SessionTypeDialogComponent } from './session-type-dialog.component';

@Component({
  selector: 'app-tables',
  standalone: true,
  imports: [TableCardComponent, LoadingSpinnerComponent],
  templateUrl: './tables.component.html',
})
export class TablesComponent implements OnInit {
  private tableService = inject(TableService);
  private socket = inject(SocketService);
  private toast = inject(ToastService);
  private cart = inject(CartStore);
  private dialog = inject(MatDialog);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  readonly tables = signal<Table[]>([]);
  readonly isLoading = signal(true);

  ngOnInit(): void {
    this.loadTables();

    this.socket
      .subscribe<Table[]>('/topic/tables')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadTables());
  }

  private loadTables(): void {
    this.isLoading.set(true);
    this.tableService.getTables().subscribe({
      next: tables => this.tables.set(tables),
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  async onTableTap(table: Table): Promise<void> {
    if (table.status === 'AVAILABLE') {
      const ref = this.dialog.open(SessionTypeDialogComponent);
      const sessionType = await firstValueFrom(ref.afterClosed());
      if (!sessionType) return;

      // No order is created yet — an empty order would occupy the table for nothing.
      // The order is only created once the waiter actually sends items to the kitchen.
      this.cart.startSession(table.id, table.tableNumber, sessionType);
      this.router.navigate(['/menu', table.id]);
      return;
    }

    if ((table.status === 'OCCUPIED' || table.status === 'ORDER_READY') && table.currentOrderId) {
      this.router.navigate(['/menu', table.id]);
      return;
    }

    if (table.status === 'READY_FOR_BILL' && table.currentOrderId) {
      this.router.navigate(['/bill', table.currentOrderId]);
      return;
    }

    this.toast.info('This table is reserved.');
  }

  onViewBill(table: Table): void {
    if (table.currentOrderId) {
      this.router.navigate(['/bill', table.currentOrderId]);
    }
  }
}
