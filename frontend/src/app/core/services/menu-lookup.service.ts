import { Injectable, inject, signal } from '@angular/core';
import { MenuService } from './menu.service';

/**
 * The order/bill API doesn't reliably return each item's name (a backend gap), but the
 * menu endpoint always does. This caches menuItemId → name from the menu so every page
 * that lists order items (Kitchen, Bill, Admin billing) can resolve real names locally
 * instead of depending on the broken field.
 */
@Injectable({ providedIn: 'root' })
export class MenuLookupService {
  private menuService = inject(MenuService);
  private namesById = signal<Map<number, string>>(new Map());
  private loadStarted = false;

  ensureLoaded(): void {
    if (this.loadStarted) return;
    this.loadStarted = true;
    this.menuService.getItems().subscribe(items => {
      this.namesById.set(new Map(items.map(item => [item.id, item.name])));
    });
  }

  nameFor(menuItemId: number): string | undefined {
    return this.namesById().get(menuItemId);
  }
}
