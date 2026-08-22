import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Bill, PaymentMode } from '../models/bill.model';

@Injectable({ providedIn: 'root' })
export class BillService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/bills`;

  generateBill(orderId: number): Observable<Bill> {
    return this.http.post<ApiResponse<Bill>>(`${this.base}/generate/${orderId}`, {}).pipe(map(res => res.data));
  }

  getBill(orderId: number): Observable<Bill> {
    return this.http.get<ApiResponse<Bill>>(`${this.base}/order/${orderId}`).pipe(map(res => res.data));
  }

  markPaid(billId: number, paymentMode: PaymentMode): Observable<Bill> {
    return this.http.patch<ApiResponse<Bill>>(`${this.base}/${billId}/pay`, { paymentMode }).pipe(map(res => res.data));
  }
}
