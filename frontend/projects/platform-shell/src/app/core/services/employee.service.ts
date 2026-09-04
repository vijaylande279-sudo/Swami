import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { EmployeeResponse, InviteResponse } from '../models/tenant.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);

  listEmployees(tenantId: string) {
    return this.http.get<EmployeeResponse[]>(`${environment.apiUrl}/tenants/${tenantId}/employees`);
  }

  listInvites(tenantId: string) {
    return this.http.get<InviteResponse[]>(`${environment.apiUrl}/tenants/${tenantId}/invites`);
  }

  invite(tenantId: string, email: string, roleName: string) {
    return this.http.post<InviteResponse>(`${environment.apiUrl}/tenants/${tenantId}/invites`, { email, roleName });
  }

  revokeInvite(inviteId: string) {
    return this.http.delete<void>(`${environment.apiUrl}/invites/${inviteId}`);
  }

  acceptInvite(token: string, password: string, fullName: string) {
    return this.http.post<EmployeeResponse>(`${environment.apiUrl}/invites/${token}/accept`, { password, fullName });
  }
}
