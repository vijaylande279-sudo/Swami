import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { AddOrderItemRequest, CreateOrderRequest, Order } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/orders`;

  createOrder(payload: CreateOrderRequest): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(this.base, payload).pipe(map(res => res.data));
  }

  getOrder(id: number): Observable<Order> {
    return this.http.get<ApiResponse<Order>>(`${this.base}/${id}`).pipe(map(res => res.data));
  }

  addItem(orderId: number, payload: AddOrderItemRequest): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.base}/${orderId}/items`, payload).pipe(map(res => res.data));
  }

  removeItem(orderId: number, orderItemId: number): Observable<Order> {
    return this.http
      .delete<ApiResponse<Order>>(`${this.base}/${orderId}/items/${orderItemId}`)
      .pipe(map(res => res.data));
  }

  sendToKitchen(orderId: number): Observable<Order> {
    return this.http.patch<ApiResponse<Order>>(`${this.base}/${orderId}/send`, {}).pipe(map(res => res.data));
  }

  markPreparing(orderId: number): Observable<Order> {
    return this.http.patch<ApiResponse<Order>>(`${this.base}/${orderId}/preparing`, {}).pipe(map(res => res.data));
  }

  markReady(orderId: number): Observable<Order> {
    return this.http.patch<ApiResponse<Order>>(`${this.base}/${orderId}/ready`, {}).pipe(map(res => res.data));
  }

  markServed(orderId: number): Observable<Order> {
    return this.http.patch<ApiResponse<Order>>(`${this.base}/${orderId}/served`, {}).pipe(map(res => res.data));
  }

  notifyReadyForBill(orderId: number): Observable<Order> {
    return this.http
      .patch<ApiResponse<Order>>(`${this.base}/${orderId}/ready-for-bill`, {})
      .pipe(map(res => res.data));
  }

  getKitchenQueue(): Observable<Order[]> {
    return this.http.get<ApiResponse<Order[]>>(`${this.base}/kitchen`).pipe(map(res => res.data));
  }

  getReadyForBillOrders(): Observable<Order[]> {
    return this.http.get<ApiResponse<Order[]>>(`${this.base}/ready-for-bill`).pipe(map(res => res.data));
  }
}
