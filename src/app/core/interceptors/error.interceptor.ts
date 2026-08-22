import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        auth.logout();
      } else if (err.status >= 500) {
        toast.error('Server error. Please try again.');
      } else if (err.status === 0) {
        toast.error('Cannot reach the server. Check your connection.');
      } else {
        toast.error(err.error?.error ?? 'Something went wrong');
      }
      return throwError(() => err);
    }),
  );
};
