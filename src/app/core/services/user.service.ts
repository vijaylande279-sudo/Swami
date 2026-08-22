import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { CreateUserRequest, User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/api/users`;

  getUsers(): Observable<User[]> {
    return this.http.get<ApiResponse<User[]>>(this.base).pipe(map(res => res.data));
  }

  createUser(payload: CreateUserRequest): Observable<User> {
    return this.http.post<ApiResponse<User>>(this.base, payload).pipe(map(res => res.data));
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`).pipe(map(() => void 0));
  }
}
