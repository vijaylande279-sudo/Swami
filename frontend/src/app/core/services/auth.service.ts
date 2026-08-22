import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { LoginRequest, LoginResponse, User, UserRole } from '../models/user.model';

const ROLE_HOME: Record<UserRole, string> = {
  ADMIN: '/admin',
  WAITER: '/tables',
  KITCHEN: '/kitchen',
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // JWT is kept in memory only — never localStorage/sessionStorage (XSS risk).
  // A page refresh clears it and sends the user back to /login by design.
  private token: string | null = null;
  private currentUser = signal<User | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => !!this.currentUser());

  login(credentials: LoginRequest): Observable<void> {
    return this.http.post<ApiResponse<LoginResponse>>(`${environment.apiUrl}/api/auth/login`, credentials).pipe(
      tap(res => {
        const { token, id, name, email, role } = res.data;
        if (!token || !role) {
          throw new Error('Login response is missing token or role.');
        }
        this.token = token;
        this.currentUser.set({ id, name, email, role });
      }),
      map(() => void 0),
    );
  }

  getToken(): string | null {
    return this.token;
  }

  redirectAfterLogin(): Promise<boolean> {
    const role = this.currentUser()?.role;
    return this.router.navigate([role ? ROLE_HOME[role] : '/tables']);
  }

  logout(): void {
    this.token = null;
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }
}
