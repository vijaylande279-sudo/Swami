import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
import { User } from '../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser: User = { id: 1, name: 'Waiter One', email: 'w@hotel.com', role: 'WAITER' };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('starts logged out with no token', () => {
    expect(service.getToken()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
  });

  it('stores the token and user in memory on successful login', () => {
    service.login({ email: mockUser.email, password: 'secret1' }).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/api/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true, data: { token: 'abc123', user: mockUser } });

    expect(service.getToken()).toBe('abc123');
    expect(service.user()).toEqual(mockUser);
    expect(service.isLoggedIn()).toBe(true);
  });

  it('clears the token and user on logout', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    service.login({ email: mockUser.email, password: 'secret1' }).subscribe();
    httpMock.expectOne(`${environment.apiUrl}/api/auth/login`).flush({
      success: true,
      data: { token: 'abc123', user: mockUser },
    });

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
  });
});
