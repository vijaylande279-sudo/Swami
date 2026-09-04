import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TenantContextService } from '../../../core/services/tenant-context.service';

@Component({
  selector: 'app-mfa-challenge',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './mfa-challenge.component.html',
})
export class MfaChallengeComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private tenantContext = inject(TenantContextService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');
  private challengeId = this.route.snapshot.queryParamMap.get('challengeId') ?? '';

  form = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit(): void {
    if (this.form.invalid || this.isSubmitting() || !this.challengeId) return;

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.auth.completeMfaLogin(this.challengeId, this.form.getRawValue().code).subscribe({
      next: () => {
        this.auth.fetchMe().subscribe(() => {
          this.tenantContext.loadCurrentTenant().subscribe();
          this.router.navigate(['/console']);
        });
      },
      error: (err: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err instanceof HttpErrorResponse ? err.error?.message ?? 'Invalid code' : 'Something went wrong');
      },
    });
  }
}
