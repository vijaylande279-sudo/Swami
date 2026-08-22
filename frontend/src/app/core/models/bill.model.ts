export type PaymentMode = 'CASH' | 'CARD' | 'UPI';

export interface BillItem {
  menuItemId: number;
  name: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface Bill {
  id: number;
  orderId: number;
  tableNumber: string;
  sessionType: string;
  waiterName: string;
  generatedAt: string;
  items: BillItem[];
  subtotal: number;
  taxPercent: number;
  taxAmount: number;
  total: number;
  paymentMode: PaymentMode | null;
  paid: boolean;
  qrCodeBase64: string | null;
}
