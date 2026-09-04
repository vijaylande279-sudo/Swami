import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EmployeeService } from '../../core/services/employee.service';
import { InviteResponse, RoleResponse } from '../../core/models/tenant.model';

@Component({
  selector: 'app-invite-employee',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './invite-employee.component.html',
})
export class InviteEmployeeComponent {
  private fb = inject(FormBuilder);
  private employeeService = inject(EmployeeService);

  @Input({ required: true }) tenantId!: string;
  @Input() roles: RoleResponse[] = [];
  @Output() invited = new EventEmitter<InviteResponse>();

  readonly isSubmitting = signal(false);
  readonly lastInvite = signal<InviteResponse | null>(null);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    roleName: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);
    const { email, roleName } = this.form.getRawValue();
    this.employeeService.invite(this.tenantId, email, roleName).subscribe({
      next: invite => {
        this.isSubmitting.set(false);
        this.lastInvite.set(invite);
        this.invited.emit(invite);
        this.form.reset();
      },
      error: () => this.isSubmitting.set(false),
    });
  }
}
