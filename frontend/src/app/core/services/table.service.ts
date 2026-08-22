import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Table } from '../models/table.model';

@Injectable({ providedIn: 'root' })
export class TableService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/tables`;

  getTables(): Observable<Table[]> {
    return this.http.get<ApiResponse<Table[]>>(this.base).pipe(map(res => res.data));
  }

  getTable(id: number): Observable<Table> {
    return this.http.get<ApiResponse<Table>>(`${this.base}/${id}`).pipe(map(res => res.data));
  }

  createTable(payload: { tableNumber: string; capacity: number }): Observable<Table> {
    return this.http.post<ApiResponse<Table>>(this.base, payload).pipe(map(res => res.data));
  }

  updateTable(id: number, payload: { tableNumber: string; capacity: number }): Observable<Table> {
    return this.http.put<ApiResponse<Table>>(`${this.base}/${id}`, payload).pipe(map(res => res.data));
  }

  deleteTable(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`).pipe(map(() => void 0));
  }
}
