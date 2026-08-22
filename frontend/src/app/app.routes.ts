import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: 'tables',
        loadComponent: () => import('./features/tables/tables.component').then(m => m.TablesComponent),
        canActivate: [roleGuard(['ADMIN', 'WAITER'])],
      },
      {
        path: 'menu/:tableId',
        loadComponent: () => import('./features/menu/menu.component').then(m => m.MenuComponent),
        canActivate: [roleGuard(['ADMIN', 'WAITER'])],
      },
      {
        path: 'bill/:orderId',
        loadComponent: () => import('./features/bill/bill.component').then(m => m.BillComponent),
        canActivate: [roleGuard(['ADMIN', 'WAITER'])],
      },
      {
        path: 'kitchen',
        loadComponent: () =>
          import('./features/kitchen/kitchen-queue/kitchen-queue.component').then(m => m.KitchenQueueComponent),
        canActivate: [roleGuard(['ADMIN', 'KITCHEN'])],
      },
      {
        path: 'admin',
        loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
        canActivate: [roleGuard(['ADMIN'])],
      },
      { path: '', redirectTo: 'tables', pathMatch: 'full' },
    ],
  },
  { path: '**', redirectTo: '' },
];
