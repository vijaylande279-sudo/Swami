import { Component } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MenuManagementComponent } from './menu-management/menu-management.component';
import { TableManagementComponent } from './table-management/table-management.component';
import { BillingManagementComponent } from './billing-management/billing-management.component';
import { StaffManagementComponent } from './staff-management/staff-management.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    MatTabsModule,
    MenuManagementComponent,
    TableManagementComponent,
    BillingManagementComponent,
    StaffManagementComponent,
  ],
  template: `
    <div class="min-h-screen bg-surface">
      <header class="bg-surface-card border-b border-surface-border px-4 py-3">
        <h1 class="text-xl md:text-2xl font-bold text-white">Admin</h1>
      </header>

      <mat-tab-group mat-stretch-tabs="false" animationDuration="150ms">
        <mat-tab label="Menu">
          <app-menu-management />
        </mat-tab>
        <mat-tab label="Tables">
          <app-table-management />
        </mat-tab>
        <mat-tab label="Billing">
          <app-billing-management />
        </mat-tab>
        <mat-tab label="Staff">
          <app-staff-management />
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
})
export class AdminComponent {}
