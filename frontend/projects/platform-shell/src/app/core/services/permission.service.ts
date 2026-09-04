import { Injectable, inject } from '@angular/core';
import { AuthService } from './auth.service';

/** Thin, readable facade over AuthService's permission/role signals — what the has-permission directive and route guards read. */
@Injectable({ providedIn: 'root' })
export class PermissionService {
  private auth = inject(AuthService);

  readonly permissions = this.auth.permissions;
  readonly roles = this.auth.roles;

  has(permission: string): boolean {
    return this.auth.hasPermission(permission);
  }

  hasAny(permissions: string[]): boolean {
    return permissions.some(p => this.has(p));
  }
}
