import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { CheckoutResponse, CheckoutStatusResponse, InvoiceResponse } from '../models/billing.model';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private http = inject(HttpClient);

  /** Body carries only an appKey - there is no amount field to send, let alone trust; the server computes the price itself. */
  checkout(appKey: string) {
    return this.http.post<CheckoutResponse>(`${environment.apiUrl}/payments/checkout`, { appKey });
  }

  /** Only ever reads state the webhook already wrote - never call this to "confirm" a payment. */
  status(checkoutIntentId: string) {
    return this.http.get<CheckoutStatusResponse>(`${environment.apiUrl}/payments/status/${checkoutIntentId}`);
  }

  listInvoices() {
    return this.http.get<InvoiceResponse[]>(`${environment.apiUrl}/payments/invoices`);
  }
}
