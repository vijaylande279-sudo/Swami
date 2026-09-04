import { HttpClient, HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';
import { TokenPairResponse } from '../models/auth.model';

const SKIP_REFRESH = new HttpContextToken(() => false);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const http = inject(HttpClient);

  const token = auth.getAccessToken();
  const authedReq = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  const isAuthEndpoint = req.url.startsWith(`${environment.apiUrl}/auth/`);

  return next(authedReq).pipe(
    catchError((error: unknown) => {
      const canRetry = error instanceof HttpErrorResponse && error.status === 401 && !isAuthEndpoint && !req.context.get(SKIP_REFRESH);
      const refreshToken = auth.getRefreshToken();
      if (!canRetry || !refreshToken) {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          auth.logout();
        }
        return throwError(() => error);
      }

      // Refresh once, then retry the original request with the new token.
      return http
        .post<TokenPairResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken }, { context: req.context.set(SKIP_REFRESH, true) })
        .pipe(
          switchMap(tokens => {
            auth.applyTokens(tokens);
            const retried = req.clone({ setHeaders: { Authorization: `Bearer ${tokens.accessToken}` } });
            return next(retried);
          }),
          catchError(refreshError => {
            auth.logout();
            router.navigate(['/login']);
            return throwError(() => refreshError);
          }),
        );
    }),
  );
};
