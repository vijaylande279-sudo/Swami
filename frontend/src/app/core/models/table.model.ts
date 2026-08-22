export type TableStatus = 'AVAILABLE' | 'OCCUPIED' | 'RESERVED' | 'ORDER_READY' | 'READY_FOR_BILL';

export interface Table {
  id: number;
  tableNumber: string;
  capacity: number;
  status: TableStatus;
  currentOrderId: number | null;
}
