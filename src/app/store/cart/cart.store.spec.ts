import { TestBed } from '@angular/core/testing';
import { CartStore } from './cart.store';
import { MenuItem } from '../../core/models/menu.model';

describe('CartStore', () => {
  let store: CartStore;

  const item: MenuItem = {
    id: 1,
    categoryId: 1,
    name: 'Paneer Tikka',
    description: '',
    price: 250,
    imageUrl: null,
    available: true,
    vegetarian: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    store = TestBed.inject(CartStore);
    store.startSession(1, 'T1', 'LUNCH');
  });

  it('increments quantity instead of creating a duplicate line when the same item is added twice', () => {
    store.addItem(item);
    store.addItem(item);

    expect(store.items().length).toBe(1);
    expect(store.items()[0].quantity).toBe(2);
  });

  it('removes the item once its quantity is decremented below 1', () => {
    store.addItem(item);
    store.decrementItem(item.id, '');

    expect(store.items().length).toBe(0);
  });

  it('computes the estimated total from quantity and unit price', () => {
    store.addItem(item);
    store.addItem(item);

    expect(store.estimatedTotal()).toBe(500);
  });

  it('clears all state on clear()', () => {
    store.addItem(item);
    store.clear();

    expect(store.items().length).toBe(0);
    expect(store.tableId()).toBeNull();
  });
});
