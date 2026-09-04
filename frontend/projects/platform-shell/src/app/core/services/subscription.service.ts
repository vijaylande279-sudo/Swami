import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { SubscriptionResponse } from '../models/billing.model';

@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private http = inject(HttpClient);

  list() {
    return this.http.get<SubscriptionResponse[]>(`${environment.apiUrl}/subscriptions`);
  }

  cancel(subscriptionId: string) {
    return this.http.post<void>(`${environment.apiUrl}/subscriptions/${subscriptionId}/cancel`, {});
  }
}
