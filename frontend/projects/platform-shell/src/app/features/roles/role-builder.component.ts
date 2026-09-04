import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RoleService } from '../../core/services/role.service';
import { PermissionResponse, RoleResponse } from '../../core/models/tenant.model';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

@Component({
  selector: 'app-role-builder',
  standalone: true,
  imports: [ReactiveFormsModule, HasPermissionDirective],
  templateUrl: './role-builder.component.html',
})
export class RoleBuilderComponent implements OnInit {
  private fb = inject(FormBuilder);
  private roleService = inject(RoleService);

  readonly roles = signal<RoleResponse[]>([]);
  readonly permissions = signal<PermissionResponse[]>([]);
  readonly isSubmitting = signal(false);
  readonly selectedPermissions = signal<Set<string>>(new Set());

  form = this.fb.nonNullable.group({ name: ['', Validators.required] });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.roleService.listRoles().subscribe(roles => this.roles.set(roles));
    this.roleService.listPermissions().subscribe(permissions => this.permissions.set(permissions));
  }

  togglePermission(code: string, checked: boolean): void {
    const next = new Set(this.selectedPermissions());
    checked ? next.add(code) : next.delete(code);
    this.selectedPermissions.set(next);
  }

  createRole(): void {
    if (this.form.invalid || this.isSubmitting()) return;
    this.isSubmitting.set(true);
    this.roleService.createRole(this.form.getRawValue().name, [...this.selectedPermissions()]).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.form.reset();
        this.selectedPermissions.set(new Set());
        this.reload();
      },
      error: () => this.isSubmitting.set(false),
    });
  }

  deleteRole(roleId: string): void {
    this.roleService.deleteRole(roleId).subscribe(() => this.reload());
  }
}
