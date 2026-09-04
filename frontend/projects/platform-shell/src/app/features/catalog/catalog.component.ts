import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CatalogService } from '../../core/services/catalog.service';
import { AppSummary } from '../../core/models/catalog.model';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './catalog.component.html',
})
export class CatalogComponent implements OnInit {
  private catalogService = inject(CatalogService);
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly apps = signal<AppSummary[]>([]);

  ngOnInit(): void {
    this.catalogService.listApps().subscribe(apps => this.apps.set(apps));
  }

  subscribe(appKey: string): void {
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/console/checkout', appKey]);
    } else {
      this.router.navigate(['/register'], { queryParams: { subscribeTo: appKey } });
    }
  }

  formatRupees(paise: number): string {
    return (paise / 100).toLocaleString('en-IN', { maximumFractionDigits: 0 });
  }
}
