import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { MenuCategory, MenuItem } from '../models/menu.model';

@Injectable({ providedIn: 'root' })
export class MenuService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/menu`;

  getCategories(): Observable<MenuCategory[]> {
    return this.http.get<ApiResponse<MenuCategory[]>>(`${this.base}/categories`).pipe(map(res => res.data));
  }

  getItems(): Observable<MenuItem[]> {
    return this.http.get<ApiResponse<MenuItem[]>>(`${this.base}/items`).pipe(map(res => res.data));
  }

  createItem(payload: Omit<MenuItem, 'id'>): Observable<MenuItem> {
    return this.http.post<ApiResponse<MenuItem>>(`${this.base}/items`, payload).pipe(map(res => res.data));
  }

  updateItem(id: number, payload: Omit<MenuItem, 'id'>): Observable<MenuItem> {
    return this.http.put<ApiResponse<MenuItem>>(`${this.base}/items/${id}`, payload).pipe(map(res => res.data));
  }

  deleteItem(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/items/${id}`).pipe(map(() => void 0));
  }

  setAvailability(id: number, available: boolean): Observable<MenuItem> {
    return this.http
      .patch<ApiResponse<MenuItem>>(`${this.base}/items/${id}/availability`, { available })
      .pipe(map(res => res.data));
  }
}
