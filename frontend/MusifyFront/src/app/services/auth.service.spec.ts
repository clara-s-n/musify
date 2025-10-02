import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    // Mock sessionStorage
    const mockSessionStorage = {
      getItem: jasmine.createSpy('getItem').and.returnValue(null),
      setItem: jasmine.createSpy('setItem'),
      removeItem: jasmine.createSpy('removeItem')
    };

    spyOn(window, 'sessionStorage').and.returnValue(mockSessionStorage);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should return false for isAuthenticated when no token exists', () => {
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('should authenticate user and store token on successful login', () => {
    // Test data
    const email = 'test@example.com';
    const password = 'password123';
    const mockResponse = {
      status: 'success',
      message: 'Login successful',
      data: {
        accessToken: 'mock-jwt-token'
      }
    };

    // Call login method
    let result: any;
    service.login(email, password).subscribe(res => {
      result = res;
    });

    // Expect a request to the login endpoint
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email, password });

    // Respond with mock data
    req.flush(mockResponse);

    // Verify token was stored
    expect(window.sessionStorage().setItem).toHaveBeenCalledWith('auth_token', 'mock-jwt-token');
    expect(result).toEqual(mockResponse.data);
  });

  it('should clear token and user data on logout', () => {
    // Setup: simulate a logged-in state
    spyOn(service, 'getToken').and.returnValue('mock-token');

    // Call logout
    service.logout();

    // Verify token was removed
    expect(window.sessionStorage().removeItem).toHaveBeenCalledWith('auth_token');
  });
});