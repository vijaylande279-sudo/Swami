import { Injectable, computed, signal } from '@angular/core';
import { MenuItem } from '../../core/models/menu.model';
import { SessionType } from '../../core/models/order.model';

export interface CartItem {
  menuItemId: number;
  name: string;
  quantity: number;
  unitPrice: number;
  notes: string;
}

interface CartState {
  tableId: number | null;
  tableNumber: string | null;
  sessionType: SessionType | null;
  orderId: number | null;
  items: CartItem[];
}

const EMPTY_CART: CartState = {
  tableId: null,
  tableNumber: null,
  sessionType: null,
  orderId: null,
  items: [],
};

/**
 * Cart state as Angular Signals (chosen over NgRx for this app — a single
 * mutable cart doesn't need action/reducer ceremony). Kept consistent as the
 * only state-management approach across the codebase.
 */
@Injectable({ providedIn: 'root' })
export class CartStore {
  private state = signal<CartState>(EMPTY_CART);

  readonly tableId = computed(() => this.state().tableId);
  readonly tableNumber = computed(() => this.state().tableNumber);
  readonly sessionType = computed(() => this.state().sessionType);
  readonly orderId = computed(() => this.state().orderId);
  readonly items = computed(() => this.state().items);
  readonly itemCount = computed(() => this.state().items.reduce((sum, i) => sum + i.quantity, 0));
  // Estimate only — the server is always the source of truth for billing totals.
  readonly estimatedTotal = computed(() =>
    this.state().items.reduce((sum, i) => sum + i.quantity * i.unitPrice, 0),
  );

  startSession(tableId: number, tableNumber: string, sessionType: SessionType): void {
    this.state.set({ ...EMPTY_CART, tableId, tableNumber, sessionType });
  }

  setOrderId(orderId: number): void {
    this.state.update(s => ({ ...s, orderId }));
  }

  addItem(item: MenuItem, notes = ''): void {
    this.state.update(s => {
      const existing = s.items.find(i => i.menuItemId === item.id && i.notes === notes);
      if (existing) {
        return {
          ...s,
          items: s.items.map(i => (i === existing ? { ...i, quantity: i.quantity + 1 } : i)),
        };
      }
      return {
        ...s,
        items: [...s.items, { menuItemId: item.id, name: item.name, quantity: 1, unitPrice: item.price, notes }],
      };
    });
  }

  incrementItem(menuItemId: number, notes: string): void {
    this.state.update(s => ({
      ...s,
      items: s.items.map(i => (i.menuItemId === menuItemId && i.notes === notes ? { ...i, quantity: i.quantity + 1 } : i)),
    }));
  }

  decrementItem(menuItemId: number, notes: string): void {
    this.state.update(s => ({
      ...s,
      items: s.items
        .map(i => (i.menuItemId === menuItemId && i.notes === notes ? { ...i, quantity: i.quantity - 1 } : i))
        .filter(i => i.quantity > 0),
    }));
  }

  removeItem(menuItemId: number, notes: string): void {
    this.state.update(s => ({
      ...s,
      items: s.items.filter(i => !(i.menuItemId === menuItemId && i.notes === notes)),
    }));
  }

  removeAllForItem(menuItemId: number): void {
    this.state.update(s => ({
      ...s,
      items: s.items.filter(i => i.menuItemId !== menuItemId),
    }));
  }

  updateNotes(menuItemId: number, oldNotes: string, newNotes: string): void {
    this.state.update(s => {
      const line = s.items.find(i => i.menuItemId === menuItemId && i.notes === oldNotes);
      if (!line) return s;
      const target = s.items.find(i => i.menuItemId === menuItemId && i.notes === newNotes);

      if (target && target !== line) {
        return {
          ...s,
          items: s.items
            .map(i => (i === target ? { ...i, quantity: i.quantity + line.quantity } : i))
            .filter(i => i !== line),
        };
      }
      return { ...s, items: s.items.map(i => (i === line ? { ...i, notes: newNotes } : i)) };
    });
  }

  clear(): void {
    this.state.set(EMPTY_CART);
  }
}
