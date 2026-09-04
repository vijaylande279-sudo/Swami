import { Component, OnInit, inject, signal } from '@angular/core';
import { EmployeeService } from '../../core/services/employee.service';
import { RoleService } from '../../core/services/role.service';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeResponse, InviteResponse, RoleResponse } from '../../core/models/tenant.model';
import { InviteEmployeeComponent } from './invite-employee.component';
import { HasPermissionDirective } from '../../directives/has-permission.directive';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [InviteEmployeeComponent, HasPermissionDirective],
  templateUrl: './employee-list.component.html',
})
export class EmployeeListComponent implements OnInit {
  private employeeService = inject(EmployeeService);
  private roleService = inject(RoleService);
  private auth = inject(AuthService);

  readonly employees = signal<EmployeeResponse[]>([]);
  readonly invites = signal<InviteResponse[]>([]);
  readonly roles = signal<RoleResponse[]>([]);

  ngOnInit(): void {
    this.reload();
    this.roleService.listRoles().subscribe(roles => this.roles.set(roles));
  }

  reload(): void {
    const tenantId = this.auth.tenantId();
    if (!tenantId) return;
    this.employeeService.listEmployees(tenantId).subscribe(employees => this.employees.set(employees));
    this.employeeService.listInvites(tenantId).subscribe(invites => this.invites.set(invites));
  }

  revoke(inviteId: string): void {
    this.employeeService.revokeInvite(inviteId).subscribe(() => this.reload());
  }

  get tenantId(): string {
    return this.auth.tenantId() ?? '';
  }
}
