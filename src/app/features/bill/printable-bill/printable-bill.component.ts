import { Component, Input, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Bill, BillItem } from '../../../core/models/bill.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { MenuLookupService } from '../../../core/services/menu-lookup.service';

@Component({
  selector: 'app-printable-bill',
  standalone: true,
  imports: [DatePipe, CurrencyFormatPipe],
  templateUrl: './printable-bill.component.html',
})
export class PrintableBillComponent {
  @Input({ required: true }) bill!: Bill;

  private menuLookup = inject(MenuLookupService);

  itemName(item: BillItem): string {
    return item.name || this.menuLookup.nameFor(item.menuItemId) || `Item #${item.menuItemId}`;
  }
}
