import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { permissionGuard } from './core/guards/permission.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/catalog/catalog.component').then(m => m.CatalogComponent),
  },
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
    path: 'console',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
  },
  {
    path: 'console/mfa-enrollment',
    canActivate: [authGuard],
    loadComponent: () => import('./features/mfa-enrollment/mfa-enrollment.component').then(m => m.MfaEnrollmentComponent),
  },
  {
    path: 'console/employees',
    canActivate: [authGuard, permissionGuard('tenant:employee:read')],
    loadComponent: () => import('./features/employees/employee-list.component').then(m => m.EmployeeListComponent),
  },
  {
    path: 'console/roles',
    canActivate: [authGuard, permissionGuard('tenant:role:read')],
    loadComponent: () => import('./features/roles/role-builder.component').then(m => m.RoleBuilderComponent),
  },
  {
    path: 'console/checkout/:appKey',
    canActivate: [authGuard, permissionGuard('platform:billing:purchase')],
    loadComponent: () => import('./features/billing/checkout/checkout.component').then(m => m.CheckoutComponent),
  },
  {
    path: 'console/billing/status/:checkoutIntentId',
    canActivate: [authGuard],
    loadComponent: () => import('./features/billing/status/payment-status.component').then(m => m.PaymentStatusComponent),
  },
  {
    path: 'console/billing',
    canActivate: [authGuard],
    loadComponent: () => import('./features/billing/billing-overview.component').then(m => m.BillingOverviewComponent),
  },
  { path: '**', redirectTo: '' },
];
