import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PaymentService } from '../../core/services/payment.service';
import { SubscriptionService } from '../../core/services/subscription.service';
import { InvoiceResponse, SubscriptionResponse } from '../../core/models/billing.model';

@Component({
  selector: 'app-billing-overview',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './billing-overview.component.html',
})
export class BillingOverviewComponent implements OnInit {
  private paymentService = inject(PaymentService);
  private subscriptionService = inject(SubscriptionService);

  readonly subscriptions = signal<SubscriptionResponse[]>([]);
  readonly invoices = signal<InvoiceResponse[]>([]);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.subscriptionService.list().subscribe(subs => this.subscriptions.set(subs));
    this.paymentService.listInvoices().subscribe(invoices => this.invoices.set(invoices));
  }

  cancel(subscriptionId: string): void {
    this.subscriptionService.cancel(subscriptionId).subscribe(() => this.reload());
  }

  formatRupees(paise: number): string {
    return (paise / 100).toLocaleString('en-IN', { maximumFractionDigits: 0 });
  }
}
