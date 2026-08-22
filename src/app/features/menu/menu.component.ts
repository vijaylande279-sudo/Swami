import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatBottomSheet } from '@angular/material/bottom-sheet';
import { MatIconModule } from '@angular/material/icon';
import { forkJoin } from 'rxjs';
import { MenuService } from '../../core/services/menu.service';
import { OrderService } from '../../core/services/order.service';
import { TableService } from '../../core/services/table.service';
import { ToastService } from '../../core/services/toast.service';
import { CartStore } from '../../store/cart/cart.store';
import { MenuItem, MenuCategory } from '../../core/models/menu.model';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { MenuCategoryComponent } from './menu-category/menu-category.component';
import { MenuItemCardComponent } from './menu-item-card/menu-item-card.component';
import { OrderCartComponent } from './order-cart/order-cart.component';
import { CurrencyFormatPipe } from '../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    LoadingSpinnerComponent,
    MenuCategoryComponent,
    MenuItemCardComponent,
    MatIconModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './menu.component.html',
})
export class MenuComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private menuService = inject(MenuService);
  private orderService = inject(OrderService);
  private tableService = inject(TableService);
  private toast = inject(ToastService);
  private confirmDialog = inject(ConfirmDialogService);
  private bottomSheet = inject(MatBottomSheet);
  cart = inject(CartStore);

  readonly isLoading = signal(true);
  readonly isSending = signal(false);
  readonly categories = signal<MenuCategory[]>([]);
  readonly items = signal<MenuItem[]>([]);
  readonly selectedCategoryId = signal<number | null>(null);

  readonly filteredItems = computed(() => {
    const categoryId = this.selectedCategoryId();
    return categoryId === null ? this.items() : this.items().filter(i => i.categoryId === categoryId);
  });

  readonly selectedItemIds = computed(() => new Set(this.cart.items().map(i => i.menuItemId)));

  ngOnInit(): void {
    const tableId = Number(this.route.snapshot.paramMap.get('tableId'));

    if (this.cart.tableId() !== tableId) {
      this.initSessionFromServer(tableId);
    }

    forkJoin({
      categories: this.menuService.getCategories(),
      items: this.menuService.getItems(),
    }).subscribe({
      next: ({ categories, items }) => {
        this.categories.set(categories);
        this.items.set(items);
      },
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  private initSessionFromServer(tableId: number): void {
    this.tableService.getTable(tableId).subscribe(table => {
      if (table.currentOrderId) {
        this.orderService.getOrder(table.currentOrderId).subscribe(order => {
          this.cart.startSession(table.id, table.tableNumber, order.sessionType);
          this.cart.setOrderId(order.id);
        });
      } else {
        this.router.navigate(['/tables']);
      }
    });
  }

  selectCategory(categoryId: number | null): void {
    this.selectedCategoryId.set(categoryId);
  }

  toggleItem(item: MenuItem): void {
    if (this.selectedItemIds().has(item.id)) {
      this.cart.removeAllForItem(item.id);
    } else {
      this.cart.addItem(item);
    }
  }

  openCart(): void {
    const ref = this.bottomSheet.open(OrderCartComponent);
    ref.instance.sendToKitchen.subscribe(() => {
      this.sendToKitchen();
      ref.dismiss();
    });
  }

  async sendToKitchen(): Promise<void> {
    const tableId = this.cart.tableId();
    const sessionType = this.cart.sessionType();
    if (!tableId || !sessionType || this.cart.items().length === 0 || this.isSending()) return;

    const confirmed = await this.confirmDialog.open('Send this order to the kitchen?', 'Send to Kitchen');
    if (!confirmed) return;

    this.isSending.set(true);

    // The order is created lazily, right here, so a waiter who picks a session type
    // but never adds an item never occupies the table with an empty order.
    const existingOrderId = this.cart.orderId();
    const order$ = existingOrderId
      ? this.orderService.getOrder(existingOrderId)
      : this.orderService.createOrder({ tableId, sessionType });

    order$.subscribe({
      next: order => this.addItemsAndSend(order.id),
      error: () => this.isSending.set(false),
    });
  }

  private addItemsAndSend(orderId: number): void {
    this.cart.setOrderId(orderId);
    const items = this.cart.items();
    forkJoin(
      items.map(item =>
        this.orderService.addItem(orderId, {
          menuItemId: item.menuItemId,
          quantity: item.quantity,
          notes: item.notes,
        }),
      ),
    ).subscribe({
      next: () => {
        this.orderService.sendToKitchen(orderId).subscribe({
          next: () => {
            this.toast.success('Order sent to kitchen!');
            this.cart.clear();
            this.router.navigate(['/tables']);
          },
          error: () => this.isSending.set(false),
          complete: () => this.isSending.set(false),
        });
      },
      error: () => this.isSending.set(false),
    });
  }
}
