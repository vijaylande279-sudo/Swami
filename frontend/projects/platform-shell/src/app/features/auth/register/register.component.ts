import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TenantContextService } from '../../../core/services/tenant-context.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private tenantContext = inject(TenantContextService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private subscribeTo = this.route.snapshot.queryParamMap.get('subscribeTo');

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');

  form = this.fb.nonNullable.group({
    tenantName: ['', Validators.required],
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    const { tenantName, email, password, fullName } = this.form.getRawValue();

    this.auth.register(tenantName, email, password, fullName).subscribe({
      next: () => {
        this.auth.fetchMe().subscribe(() => {
          this.tenantContext.loadCurrentTenant().subscribe();
          if (this.subscribeTo) {
            this.router.navigate(['/console/checkout', this.subscribeTo]);
          } else {
            this.router.navigate(['/console']);
          }
        });
      },
      error: (err: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(this.extractErrorMessage(err));
      },
    });
  }

  private extractErrorMessage(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      return err.error?.message ?? 'Could not create your account';
    }
    return 'Something went wrong. Please try again.';
  }
}
