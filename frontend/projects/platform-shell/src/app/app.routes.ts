import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { permissionGuard } from './core/guards/permission.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent),
  },
  {
    path: 'mfa-challenge',
    loadComponent: () => import('./features/auth/mfa-challenge/mfa-challenge.component').then(m => m.MfaChallengeComponent),
  },
  {
    path: 'password-reset',
    loadComponent: () => import('./features/auth/password-reset/password-reset.component').then(m => m.PasswordResetComponent),
  },
  {
    path: 'accept-invite',
    loadComponent: () => import('./features/employees/accept-invite.component').then(m => m.AcceptInviteComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
  },
  {
    path: 'mfa-enrollment',
    canActivate: [authGuard],
    loadComponent: () => import('./features/mfa-enrollment/mfa-enrollment.component').then(m => m.MfaEnrollmentComponent),
  },
  {
    path: 'employees',
    canActivate: [authGuard, permissionGuard('tenant:employee:read')],
    loadComponent: () => import('./features/employees/employee-list.component').then(m => m.EmployeeListComponent),
  },
  {
    path: 'roles',
    canActivate: [authGuard, permissionGuard('tenant:role:read')],
    loadComponent: () => import('./features/roles/role-builder.component').then(m => m.RoleBuilderComponent),
  },
  { path: '**', redirectTo: '' },
];
