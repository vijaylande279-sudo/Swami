import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PermissionResponse, RoleResponse } from '../models/tenant.model';

@Injectable({ providedIn: 'root' })
export class RoleService {
  private http = inject(HttpClient);

  listRoles() {
    return this.http.get<RoleResponse[]>(`${environment.apiUrl}/roles`);
  }

  listPermissions() {
    return this.http.get<PermissionResponse[]>(`${environment.apiUrl}/permissions`);
  }

  createRole(name: string, permissionCodes: string[]) {
    return this.http.post<RoleResponse>(`${environment.apiUrl}/roles`, { name, permissionCodes });
  }

  updatePermissions(roleId: string, permissionCodes: string[]) {
    return this.http.put<RoleResponse>(`${environment.apiUrl}/roles/${roleId}/permissions`, { permissionCodes });
  }

  deleteRole(roleId: string) {
    return this.http.delete<void>(`${environment.apiUrl}/roles/${roleId}`);
  }
}
