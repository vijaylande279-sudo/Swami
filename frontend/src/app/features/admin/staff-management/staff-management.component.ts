import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog.service';
import { User, UserRole } from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';

const ROLES: UserRole[] = ['WAITER', 'KITCHEN', 'ADMIN'];

@Component({
  selector: 'app-staff-management',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    LoadingSpinnerComponent,
  ],
  templateUrl: './staff-management.component.html',
})
export class StaffManagementComponent implements OnInit {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private confirmDialog = inject(ConfirmDialogService);

  readonly roles = ROLES;
  readonly currentUserId = this.auth.user()?.id;
  readonly isLoading = signal(true);
  readonly isSubmitting = signal(false);
  readonly users = signal<User[]>([]);

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['WAITER' as UserRole, Validators.required],
  });

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.userService.getUsers().subscribe({
      next: users => this.users.set(users),
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    this.isSubmitting.set(true);
    this.userService.createUser(this.form.getRawValue()).subscribe({
      next: () => {
        this.toast.success('Staff account created.');
        this.form.reset({ name: '', email: '', password: '', role: 'WAITER' });
        this.load();
      },
      error: () => this.isSubmitting.set(false),
      complete: () => this.isSubmitting.set(false),
    });
  }

  async remove(user: User): Promise<void> {
    const confirmed = await this.confirmDialog.open(
      `Remove ${user.name}'s account? They will no longer be able to sign in.`,
      'Remove Staff',
    );
    if (!confirmed) return;

    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.users.update(users => users.filter(u => u.id !== user.id));
        this.toast.success('Staff account removed.');
      },
      error: () => {},
    });
  }
}
