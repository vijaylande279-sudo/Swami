import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MfaService } from '../../../core/services/mfa.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-password-reset',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './password-reset.component.html',
})
export class PasswordResetComponent {
  private fb = inject(FormBuilder);
  private mfaService = inject(MfaService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly isSubmitting = signal(false);
  readonly token = this.route.snapshot.queryParamMap.get('token');

  requestForm = this.fb.nonNullable.group({ email: ['', [Validators.required, Validators.email]] });
  confirmForm = this.fb.nonNullable.group({ newPassword: ['', [Validators.required, Validators.minLength(8)]] });

  requestReset(): void {
    if (this.requestForm.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);
    this.mfaService.forgotPassword(this.requestForm.getRawValue().email).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toast.info('If that email exists, a reset link has been sent (check the server logs in dev mode).');
      },
      error: () => this.isSubmitting.set(false),
    });
  }

  confirmReset(): void {
    if (this.confirmForm.invalid || this.isSubmitting() || !this.token) return;
    this.isSubmitting.set(true);
    this.mfaService.resetPassword(this.token, this.confirmForm.getRawValue().newPassword).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toast.success('Password updated — sign in with your new password.');
        this.router.navigate(['/login']);
      },
      error: () => this.isSubmitting.set(false),
    });
  }
}
