import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { PermissionService } from '../services/permission.service';

/** Route-level permission check, for UX only — the real enforcement is server-side @PreAuthorize. */
export function permissionGuard(permission: string): CanActivateFn {
  return () => {
    const permissions = inject(PermissionService);
    const router = inject(Router);
    return permissions.has(permission) ? true : router.createUrlTree(['/']);
  };
}
