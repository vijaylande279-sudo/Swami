import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MenuItem } from '../../../core/models/menu.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-menu-item-card',
  standalone: true,
  imports: [CurrencyFormatPipe, MatIconModule],
  template: `
    <button
      type="button"
      class="w-full min-h-tap text-left flex items-center gap-3 rounded-xl border bg-surface-card p-3 shadow-sm transition active:scale-[0.98] disabled:opacity-40 focus:outline-none focus:ring-2 focus:ring-brand-500"
      [class.border-brand-500]="selected"
      [class.border-surface-border]="!selected"
      (click)="toggle.emit(item)"
      [disabled]="!item.available"
    >
      <span
        class="mt-1 inline-block h-3 w-3 shrink-0 rounded-sm border"
        [class]="item.vegetarian ? 'border-green-600 bg-green-500' : 'border-red-600 bg-red-500'"
        [attr.aria-label]="item.vegetarian ? 'Vegetarian' : 'Non-vegetarian'"
      ></span>
      <span class="flex-1 min-w-0">
        <span class="block text-base font-semibold text-white truncate">{{ item.name }}</span>
        @if (item.description) {
          <span class="block text-sm text-gray-400 truncate">{{ item.description }}</span>
        }
        <span class="block text-base font-medium text-gray-200 mt-1">{{ item.price | currencyFormat }}</span>
        @if (!item.available) {
          <span class="block text-sm text-red-400">Currently unavailable</span>
        }
      </span>
      <mat-icon
        class="shrink-0"
        [class.text-brand-500]="selected"
        [class.text-gray-500]="!selected"
        [attr.aria-label]="selected ? 'Selected — tap to remove' : 'Tap to select'"
      >
        {{ selected ? 'check_circle' : 'add_circle_outline' }}
      </mat-icon>
    </button>
  `,
})
export class MenuItemCardComponent {
  @Input({ required: true }) item!: MenuItem;
  @Input({ required: true }) selected!: boolean;
  @Output() toggle = new EventEmitter<MenuItem>();
}
