import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TenantContextService } from '../../core/services/tenant-context.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent {
  protected auth = inject(AuthService);
  protected tenant = inject(TenantContextService);
}
