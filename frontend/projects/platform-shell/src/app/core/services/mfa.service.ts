import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { MfaEnrollResponse, MfaVerifyResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class MfaService {
  private http = inject(HttpClient);

  enroll() {
    return this.http.post<MfaEnrollResponse>(`${environment.apiUrl}/mfa/enroll`, {});
  }

  verify(code: string) {
    return this.http.post<MfaVerifyResponse>(`${environment.apiUrl}/mfa/verify`, { code });
  }

  disable(password: string) {
    return this.http.post<void>(`${environment.apiUrl}/mfa/disable`, { password });
  }

  forgotPassword(email: string) {
    return this.http.post<void>(`${environment.apiUrl}/auth/password/forgot`, { email });
  }

  resetPassword(token: string, newPassword: string) {
    return this.http.post<void>(`${environment.apiUrl}/auth/password/reset`, { token, newPassword });
  }
}
