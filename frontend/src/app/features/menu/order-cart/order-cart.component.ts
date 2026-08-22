import { Component, EventEmitter, Output, inject } from '@angular/core';
import { MatBottomSheetRef } from '@angular/material/bottom-sheet';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { CartStore } from '../../../store/cart/cart.store';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { ItemNotesDialogComponent } from './item-notes-dialog.component';

@Component({
  selector: 'app-order-cart',
  standalone: true,
  imports: [MatIconModule, CurrencyFormatPipe],
  templateUrl: './order-cart.component.html',
})
export class OrderCartComponent {
  cart = inject(CartStore);
  private sheetRef = inject(MatBottomSheetRef<OrderCartComponent>);
  private dialog = inject(MatDialog);

  @Output() sendToKitchen = new EventEmitter<void>();

  increment(menuItemId: number, notes: string): void {
    this.cart.incrementItem(menuItemId, notes);
  }

  decrement(menuItemId: number, notes: string): void {
    this.cart.decrementItem(menuItemId, notes);
  }

  remove(menuItemId: number, notes: string): void {
    this.cart.removeItem(menuItemId, notes);
  }

  editNotes(menuItemId: number, currentNotes: string): void {
    const ref = this.dialog.open(ItemNotesDialogComponent, { data: currentNotes, autoFocus: false });
    ref.afterClosed().subscribe((notes: string | null) => {
      if (notes === null || notes === undefined || notes === currentNotes) return;
      this.cart.updateNotes(menuItemId, currentNotes, notes);
    });
  }

  close(): void {
    this.sheetRef.dismiss();
  }

  onSendToKitchen(): void {
    this.sendToKitchen.emit();
  }
}
