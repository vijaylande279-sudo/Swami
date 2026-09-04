import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JwtClaims, LoginResponse, MeResponse, TokenPairResponse } from '../models/auth.model';
import { decodeJwtClaims } from './jwt.util';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  // Tokens are kept in memory only — never localStorage/sessionStorage (XSS risk).
  // A page refresh clears the session and sends the user back to /login by design,
  // matching the existing app's convention (frontend/src/app/core/services/auth.service.ts).
  private accessToken: string | null = null;
  private refreshToken: string | null = null;
  private claims = signal<JwtClaims | null>(null);
  private me = signal<MeResponse | null>(null);

  // Derived from claims (a real signal), not accessToken (a plain field) — a
  // computed() only tracks other signals it reads, so basing this on the plain
  // field would evaluate once (false, before any login) and then never update.
  readonly isLoggedIn = computed(() => !!this.claims());
  readonly currentUser = this.me.asReadonly();
  readonly tenantId = computed(() => this.claims()?.tenant_id ?? null);
  readonly permissions = computed(() => this.claims()?.perms ?? []);
  readonly roles = computed(() => this.claims()?.roles ?? []);

  register(tenantName: string, email: string, password: string, fullName: string): Observable<TokenPairResponse> {
    return this.http
      .post<TokenPairResponse>(`${environment.apiUrl}/auth/register`, { tenantName, email, password, fullName })
      .pipe(tap(tokens => this.applyTokens(tokens)));
  }

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap(res => {
        if (!res.mfaRequired && res.accessToken && res.refreshToken) {
          this.applyTokens({ accessToken: res.accessToken, refreshToken: res.refreshToken });
        }
      }),
    );
  }

  completeMfaLogin(mfaChallengeId: string, code: string): Observable<TokenPairResponse> {
    return this.http
      .post<TokenPairResponse>(`${environment.apiUrl}/auth/login/mfa`, { mfaChallengeId, code })
      .pipe(tap(tokens => this.applyTokens(tokens)));
  }

  fetchMe(): Observable<MeResponse> {
    return this.http.get<MeResponse>(`${environment.apiUrl}/auth/me`).pipe(tap(me => this.me.set(me)));
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  getRefreshToken(): string | null {
    return this.refreshToken;
  }

  /** Used by the auth interceptor after a successful token refresh. */
  applyTokens(tokens: TokenPairResponse): void {
    this.accessToken = tokens.accessToken;
    this.refreshToken = tokens.refreshToken;
    this.claims.set(decodeJwtClaims(tokens.accessToken));
  }

  hasPermission(permission: string): boolean {
    return this.permissions().includes(permission);
  }

  logout(): void {
    this.accessToken = null;
    this.refreshToken = null;
    this.claims.set(null);
    this.me.set(null);
    this.router.navigate(['/login']);
  }
}
