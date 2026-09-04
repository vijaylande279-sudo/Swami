import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TenantContextService } from '../../../core/services/tenant-context.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private tenantContext = inject(TenantContextService);
  private router = inject(Router);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    const { email, password } = this.form.getRawValue();

    this.auth.login(email, password).subscribe({
      next: res => {
        this.isSubmitting.set(false);
        if (res.mfaRequired && res.mfaChallengeId) {
          this.router.navigate(['/mfa-challenge'], { queryParams: { challengeId: res.mfaChallengeId } });
          return;
        }
        this.afterLogin();
      },
      error: (err: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.extractErrorMessage(err));
      },
    });
  }

  private afterLogin(): void {
    this.auth.fetchMe().subscribe(() => {
      this.tenantContext.loadCurrentTenant().subscribe();
      this.router.navigate(['/']);
    });
  }

  private extractErrorMessage(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      return err.error?.message ?? 'Invalid email or password';
    }
    return 'Something went wrong. Please try again.';
  }
}
