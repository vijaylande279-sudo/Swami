export interface CheckoutResponse {
  checkoutIntentId: string;
  razorpayOrderId: string;
  totalPaise: number;
  razorpayKeyId: string;
}

export interface CheckoutStatusResponse {
  status: 'CREATED' | 'PAID' | 'FAILED';
}

export interface InvoiceResponse {
  id: string;
  invoiceNumber: string;
  amountPaise: number;
  gstPaise: number;
  totalPaise: number;
  issuedAt: string;
}

export interface SubscriptionResponse {
  id: string;
  appKey: string;
  status: string;
  currentPeriodStart: string | null;
  currentPeriodEnd: string | null;
}

/** Loaded dynamically from a <script> tag, never bundled via npm - Razorpay's hosted checkout, per doc §15.1. */
export interface RazorpayCheckoutOptions {
  key: string;
  order_id: string;
  amount: number;
  currency: string;
  name: string;
  description?: string;
  handler: (response: unknown) => void;
  modal?: { ondismiss?: () => void };
}

export interface RazorpayCheckoutInstance {
  open(): void;
}

declare global {
  interface Window {
    Razorpay?: new (options: RazorpayCheckoutOptions) => RazorpayCheckoutInstance;
  }
}
