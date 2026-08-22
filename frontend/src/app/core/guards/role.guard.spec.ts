import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('roleGuard', () => {
  let auth: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    auth = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  function loginAs(role: 'ADMIN' | 'WAITER' | 'KITCHEN'): void {
    auth.login({ email: 'u@hotel.com', password: 'secret1' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/api/auth/login`).flush({
      success: true,
      data: { token: 'abc123', user: { id: 1, name: 'User', email: 'u@hotel.com', role } },
    });
  }

  it('allows navigation when the user has one of the required roles', () => {
    loginAs('KITCHEN');

    const result = TestBed.runInInjectionContext(() => roleGuard(['ADMIN', 'KITCHEN'])({} as never, {} as never));

    expect(result).toBe(true);
  });

  it('redirects to /login when the user lacks the required role', () => {
    loginAs('WAITER');
    const router = TestBed.inject(Router);

    const result = TestBed.runInInjectionContext(() => roleGuard(['ADMIN'])({} as never, {} as never));

    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login');
  });
});
