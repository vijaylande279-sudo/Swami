import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TenantResponse } from '../models/tenant.model';
import { AuthService } from './auth.service';

/** Holds the current tenant's profile, fetched once after login using the JWT's tenant_id claim. */
@Injectable({ providedIn: 'root' })
export class TenantContextService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private tenant = signal<TenantResponse | null>(null);
  readonly current = this.tenant.asReadonly();

  loadCurrentTenant(): Observable<TenantResponse | null> {
    const tenantId = this.auth.tenantId();
    if (!tenantId) {
      this.tenant.set(null);
      return of(null);
    }
    return this.http
      .get<TenantResponse>(`${environment.apiUrl}/tenants/${tenantId}`)
      .pipe(tap(tenant => this.tenant.set(tenant)));
  }

  clear(): void {
    this.tenant.set(null);
  }
}
