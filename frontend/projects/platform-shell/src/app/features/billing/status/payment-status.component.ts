import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, interval, startWith, switchMap, takeWhile } from 'rxjs';
import { PaymentService } from '../../../core/services/payment.service';

/**
 * Only ever polls GET /payments/status - never a mutation. Safe to close and
 * reopen: all state lives server-side, written only by the webhook (doc §15.2).
 */
@Component({
  selector: 'app-payment-status',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './payment-status.component.html',
})
export class PaymentStatusComponent implements OnInit, OnDestroy {
  private paymentService = inject(PaymentService);
  private route = inject(ActivatedRoute);
  private polling?: Subscription;

  readonly status = signal<'CREATED' | 'PAID' | 'FAILED' | null>(null);
  private checkoutIntentId = this.route.snapshot.paramMap.get('checkoutIntentId') ?? '';

  ngOnInit(): void {
    this.polling = interval(3000)
      .pipe(
        startWith(0),
        switchMap(() => this.paymentService.status(this.checkoutIntentId)),
        takeWhile(res => res.status === 'CREATED', true),
      )
      .subscribe(res => this.status.set(res.status));
  }

  ngOnDestroy(): void {
    this.polling?.unsubscribe();
  }
}
