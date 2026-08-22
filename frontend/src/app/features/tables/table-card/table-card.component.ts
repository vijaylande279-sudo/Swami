import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Table } from '../../../core/models/table.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-table-card',
  standalone: true,
  imports: [StatusBadgeComponent, MatIconModule],
  template: `
    <div class="relative">
      <button
        type="button"
        class="w-full min-h-tap flex flex-col items-center gap-2 rounded-2xl border-2 p-4 shadow-sm transition active:scale-95 focus:outline-none focus:ring-2 focus:ring-brand-500 bg-surface-card"
        [class.border-green-500]="readyToServe()"
        [class.border-purple-400]="!readyToServe() && readyForBill()"
        [class.border-orange-400]="!readyToServe() && !readyForBill() && isOccupied()"
        [class.border-yellow-400]="!readyToServe() && !readyForBill() && table.status === 'RESERVED'"
        [class.border-surface-border]="!readyToServe() && !readyForBill() && table.status === 'AVAILABLE'"
        (click)="tap.emit(table)"
      >
        @if (readyToServe()) {
          <span class="flex items-center gap-2 px-3 py-1 bg-green-500 rounded-full">
            <span class="w-2 h-2 bg-white rounded-full animate-pulse"></span>
            <span class="text-white text-xs font-bold">Order Ready to Serve!</span>
          </span>
        } @else if (readyForBill()) {
          <span class="flex items-center gap-2 px-3 py-1 bg-purple-500 rounded-full">
            <mat-icon class="!text-sm !w-4 !h-4 text-white">receipt_long</mat-icon>
            <span class="text-white text-xs font-bold">Ready for Bill</span>
          </span>
        }

        <span class="text-xl md:text-2xl font-bold text-white">{{ table.tableNumber }}</span>
        <span class="text-sm text-gray-400">Seats {{ table.capacity }}</span>
        @if (!readyToServe() && !readyForBill()) {
          <app-status-badge [status]="table.status" />
        }
      </button>

      @if (hasActiveOrder() && table.currentOrderId) {
        <button
          type="button"
          class="absolute -top-2 -right-2 min-h-tap min-w-tap flex items-center justify-center rounded-full bg-surface-card border border-surface-border shadow-sm text-gray-300"
          (click)="viewBill.emit(table); $event.stopPropagation()"
          aria-label="View bill"
        >
          <mat-icon fontIcon="receipt_long" />
        </button>
      }
    </div>
  `,
})
export class TableCardComponent {
  @Input({ required: true }) table!: Table;
  @Output() tap = new EventEmitter<Table>();
  @Output() viewBill = new EventEmitter<Table>();

  readyToServe(): boolean {
    return this.table.status === 'ORDER_READY';
  }

  readyForBill(): boolean {
    return this.table.status === 'READY_FOR_BILL';
  }

  isOccupied(): boolean {
    return this.table.status === 'OCCUPIED' || this.table.status === 'ORDER_READY';
  }

  hasActiveOrder(): boolean {
    return this.isOccupied() || this.readyForBill();
  }
}
