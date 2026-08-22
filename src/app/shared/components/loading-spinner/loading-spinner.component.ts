import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  template: `
    <div class="flex flex-col items-center justify-center gap-3 p-8">
      <mat-spinner [diameter]="diameter"></mat-spinner>
      @if (label) {
        <p class="text-base text-gray-400">{{ label }}</p>
      }
    </div>
  `,
})
export class LoadingSpinnerComponent {
  @Input() diameter = 40;
  @Input() label = '';
}
