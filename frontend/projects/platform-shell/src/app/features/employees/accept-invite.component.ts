import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';

@Component({
  selector: 'app-accept-invite',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './accept-invite.component.html',
})
export class AcceptInviteComponent {
  private fb = inject(FormBuilder);
  private employeeService = inject(EmployeeService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');
  private token = this.route.snapshot.queryParamMap.get('token') ?? '';

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid || this.isSubmitting() || !this.token) return;
    this.isSubmitting.set(true);
    const { fullName, password } = this.form.getRawValue();
    this.employeeService.acceptInvite(this.token, password, fullName).subscribe({
      next: () => this.router.navigate(['/login']),
      error: (err: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err instanceof HttpErrorResponse ? err.error?.message ?? 'Invalid or expired invite' : 'Something went wrong');
      },
    });
  }
}
