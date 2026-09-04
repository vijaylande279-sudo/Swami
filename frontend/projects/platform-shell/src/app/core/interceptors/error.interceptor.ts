import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';

/** 401 handling lives in auth.interceptor.ts (refresh-then-retry); this only surfaces everything else. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && err.status !== 401) {
        if (err.status >= 500) {
          toast.error('Server error. Please try again.');
        } else if (err.status === 0) {
          toast.error('Cannot reach the server. Check your connection.');
        } else if (err.status !== 403) {
          toast.error(err.error?.message ?? 'Something went wrong');
        }
      }
      return throwError(() => err);
    }),
  );
};
