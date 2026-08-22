import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let auth: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    auth = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('allows navigation when the user is logged in', () => {
    auth.login({ email: 'w@hotel.com', password: 'secret1' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/api/auth/login`).flush({
      success: true,
      data: { token: 'abc123', user: { id: 1, name: 'Waiter', email: 'w@hotel.com', role: 'WAITER' } },
    });

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('redirects to /login when the user is not logged in', () => {
    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login');
  });
});
