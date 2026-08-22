export type SessionType = 'BREAKFAST' | 'LUNCH' | 'DINNER';

export type OrderStatus =
  | 'OPEN'
  | 'SENT_TO_KITCHEN'
  | 'PREPARING'
  | 'READY'
  | 'ORDER_READY'
  | 'SERVED'
  | 'READY_FOR_BILL'
  | 'BILLED'
  | 'CLOSED';

export type OrderItemStatus = 'PENDING' | 'PREPARING' | 'READY' | 'SERVED';

export interface OrderItem {
  id: number;
  menuItemId: number;
  name: string;
  quantity: number;
  unitPrice: number;
  notes: string;
  status: OrderItemStatus;
}

export interface Order {
  id: number;
  tableNumber: string;
  sessionType: SessionType;
  status: OrderStatus;
  items: OrderItem[];
  notes: string | null;
  createdAt: string;
}

export interface CreateOrderRequest {
  tableId: number;
  sessionType: SessionType;
}

export interface AddOrderItemRequest {
  menuItemId: number;
  quantity: number;
  notes: string;
}

export interface OrderReadyNotification {
  orderId: number;
  tableNumber: string;
}
