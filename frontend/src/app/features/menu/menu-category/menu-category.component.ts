import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MenuCategory } from '../../../core/models/menu.model';

@Component({
  selector: 'app-menu-category',
  standalone: true,
  template: `
    <div class="flex gap-2 overflow-x-auto px-4 py-3 -mx-4" role="tablist">
      <button
        type="button"
        role="tab"
        class="min-h-tap shrink-0 rounded-full px-4 text-sm font-medium transition"
        [class]="selectedId === null ? 'bg-brand-600 text-white' : 'bg-surface-card text-gray-300 border border-surface-border'"
        (click)="select.emit(null)"
      >
        All
      </button>
      @for (category of categories; track category.id) {
        <button
          type="button"
          role="tab"
          class="min-h-tap shrink-0 rounded-full px-4 text-sm font-medium transition"
          [class]="selectedId === category.id ? 'bg-brand-600 text-white' : 'bg-surface-card text-gray-300 border border-surface-border'"
          (click)="select.emit(category.id)"
        >
          {{ category.name }}
        </button>
      }
    </div>
  `,
})
export class MenuCategoryComponent {
  @Input() categories: MenuCategory[] = [];
  @Input() selectedId: number | null = null;
  @Output() select = new EventEmitter<number | null>();
}
