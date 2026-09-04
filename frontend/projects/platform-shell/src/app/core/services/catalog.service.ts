import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AppSummary } from '../models/catalog.model';

/** Public - unauthenticated calls, same as what the landing page reads with no bearer token. */
@Injectable({ providedIn: 'root' })
export class CatalogService {
  private http = inject(HttpClient);

  listApps() {
    return this.http.get<AppSummary[]>(`${environment.apiUrl}/catalog/apps`);
  }
}
