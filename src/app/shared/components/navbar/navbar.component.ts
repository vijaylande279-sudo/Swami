import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map } from 'rxjs';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { SocketService } from '../../../core/services/socket.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  auth = inject(AuthService);
  socket = inject(SocketService);
  private router = inject(Router);

  // Kitchen page renders its own dark navbar and must not show this one.
  private currentUrl = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => e.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );
  readonly isKitchenPage = () => this.currentUrl().startsWith('/kitchen');

  logout(): void {
    this.socket.disconnect();
    this.auth.logout();
  }
}
