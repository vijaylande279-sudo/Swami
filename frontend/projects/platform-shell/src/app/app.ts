import { Component, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';
import { TenantContextService } from './core/services/tenant-context.service';
import { HasPermissionDirective } from './directives/has-permission.directive';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, HasPermissionDirective],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected auth = inject(AuthService);
  protected tenant = inject(TenantContextService);

  logout(): void {
    this.tenant.clear();
    this.auth.logout();
  }
}
