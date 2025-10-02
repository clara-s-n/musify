import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create spies
    const authServiceSpy = jasmine.createSpyObj('AuthService',
      ['login', 'isAuthenticated'],
      { loading$: of(false) }
    );
    const routerSpy = jasmine.createSpyObj('Router', ['navigateByUrl']);

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        ReactiveFormsModule,
        LoginComponent
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParams: {}
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize login form with email and password fields', () => {
    expect(component.loginForm).toBeDefined();
    expect(component.loginForm.get('email')).toBeDefined();
    expect(component.loginForm.get('password')).toBeDefined();
  });

  it('should mark email as invalid when empty', () => {
    const emailControl = component.loginForm.get('email');
    emailControl?.setValue('');
    expect(emailControl?.valid).toBeFalsy();
  });

  it('should mark email as invalid with incorrect format', () => {
    const emailControl = component.loginForm.get('email');
    emailControl?.setValue('invalid-email');
    expect(emailControl?.valid).toBeFalsy();
  });

  it('should mark password as invalid when shorter than 6 characters', () => {
    const passwordControl = component.loginForm.get('password');
    passwordControl?.setValue('12345');
    expect(passwordControl?.valid).toBeFalsy();
  });

  it('should call auth service on form submission with valid credentials', () => {
    // Set valid form values
    component.loginForm.setValue({
      email: 'test@example.com',
      password: 'password123'
    });

    // Set up auth service to return successful login
    authService.login.and.returnValue(of({ accessToken: 'mock-token' }));

    // Trigger form submission
    component.onLogin();

    // Check that auth service was called with correct credentials
    expect(authService.login).toHaveBeenCalledWith('test@example.com', 'password123');

    // Check that user was redirected
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('should display error message on login failure', () => {
    // Set valid form values
    component.loginForm.setValue({
      email: 'test@example.com',
      password: 'password123'
    });

    // Set up auth service to return an error
    authService.login.and.returnValue(throwError(() => new Error('Invalid credentials')));

    // Trigger form submission
    component.onLogin();
    fixture.detectChanges();

    // Check that error message is displayed
    expect(component.errorMessage()).toEqual('Invalid credentials');
  });
});