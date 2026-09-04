import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MfaService } from '../../core/services/mfa.service';
import { ToastService } from '../../core/services/toast.service';
import { MfaEnrollResponse } from '../../core/models/auth.model';

@Component({
  selector: 'app-mfa-enrollment',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './mfa-enrollment.component.html',
})
export class MfaEnrollmentComponent {
  private fb = inject(FormBuilder);
  private mfaService = inject(MfaService);
  private toast = inject(ToastService);

  readonly enrollment = signal<MfaEnrollResponse | null>(null);
  readonly backupCodes = signal<string[] | null>(null);
  readonly isSubmitting = signal(false);

  verifyForm = this.fb.nonNullable.group({ code: ['', [Validators.required, Validators.minLength(6)]] });

  startEnrollment(): void {
    this.mfaService.enroll().subscribe(res => this.enrollment.set(res));
  }

  verify(): void {
    if (this.verifyForm.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);
    this.mfaService.verify(this.verifyForm.getRawValue().code).subscribe({
      next: res => {
        this.isSubmitting.set(false);
        this.backupCodes.set(res.backupCodes);
        this.toast.success('Two-factor authentication is now enabled.');
      },
      error: () => this.isSubmitting.set(false),
    });
  }
}
