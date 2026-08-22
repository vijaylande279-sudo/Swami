import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { SocketService } from '../../../core/services/socket.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, MatIconModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private socket = inject(SocketService);
  private toast = inject(ToastService);

  readonly isSubmitting = signal(false);
  readonly hidePassword = signal(true);
  readonly errorMessage = signal('');

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        const token = this.auth.getToken();
        if (token) this.socket.connect(token);
        this.auth.redirectAfterLogin().then(navigated => {
          this.isSubmitting.set(false);
          if (navigated) {
            this.toast.success(`Welcome back, ${this.auth.user()?.name ?? ''}!`);
            this.form.reset();
          } else {
            this.toast.error('Signed in, but your account could not be loaded. Please contact an administrator.');
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
      return err.error?.error ?? 'Invalid email or password';
    }
    if (err instanceof Error) return err.message;
    return 'Something went wrong. Please try again.';
  }
}
