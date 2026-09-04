import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../../core/services/payment.service';
import { RazorpayLoaderService } from '../../../core/services/razorpay-loader.service';
import { RazorpayCheckoutOptions } from '../../../core/models/billing.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent implements OnInit {
  private paymentService = inject(PaymentService);
  private razorpayLoader = inject(RazorpayLoaderService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly isStarting = signal(false);
  readonly errorMessage = signal('');
  readonly appKey = this.route.snapshot.paramMap.get('appKey') ?? '';

  ngOnInit(): void {
    this.startCheckout();
  }

  startCheckout(): void {
    this.isStarting.set(true);
    this.errorMessage.set('');

    this.paymentService.checkout(this.appKey).subscribe({
      next: checkout => {
        this.razorpayLoader.ensureLoaded().subscribe({
          next: () => this.openRazorpayCheckout(checkout),
          error: () => {
            this.isStarting.set(false);
            this.errorMessage.set('Could not load the payment window. Check your connection and try again.');
          },
        });
      },
      error: (err: unknown) => {
        this.isStarting.set(false);
        this.errorMessage.set(err instanceof HttpErrorResponse ? err.error?.message ?? 'Could not start checkout' : 'Something went wrong');
      },
    });
  }

  private openRazorpayCheckout(checkout: { checkoutIntentId: string; razorpayOrderId: string; totalPaise: number; razorpayKeyId: string }): void {
    this.isStarting.set(false);
    if (!window.Razorpay) {
      this.errorMessage.set('Payment window unavailable. Please try again.');
      return;
    }

    const options: RazorpayCheckoutOptions = {
      key: checkout.razorpayKeyId,
      order_id: checkout.razorpayOrderId,
      amount: checkout.totalPaise,
      currency: 'INR',
      name: 'Swami Suite',
      description: `Annual subscription - ${this.appKey}`,
      handler: () => {
        // Never treat this as confirmation of payment - only the webhook grants
        // access (doc §7.2/§15.1). This just moves the user to the status page,
        // which polls the server for what actually happened.
        this.router.navigate(['/console/billing/status', checkout.checkoutIntentId]);
      },
      modal: {
        ondismiss: () => this.router.navigate(['/console/billing/status', checkout.checkoutIntentId]),
      },
    };

    new window.Razorpay(options).open();
  }
}
