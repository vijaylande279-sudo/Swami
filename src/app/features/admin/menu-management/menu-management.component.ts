import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { forkJoin } from 'rxjs';
import { MenuService } from '../../../core/services/menu.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog.service';
import { MenuCategory, MenuItem } from '../../../core/models/menu.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';

@Component({
  selector: 'app-menu-management',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    LoadingSpinnerComponent,
    CurrencyFormatPipe,
  ],
  templateUrl: './menu-management.component.html',
})
export class MenuManagementComponent implements OnInit {
  private fb = inject(FormBuilder);
  private menuService = inject(MenuService);
  private toast = inject(ToastService);
  private confirmDialog = inject(ConfirmDialogService);

  readonly isLoading = signal(true);
  readonly isSubmitting = signal(false);
  readonly categories = signal<MenuCategory[]>([]);
  readonly items = signal<MenuItem[]>([]);
  readonly editingId = signal<number | null>(null);

  form = this.fb.nonNullable.group({
    categoryId: [0, [Validators.required, Validators.min(1)]],
    name: ['', Validators.required],
    description: [''],
    price: [0, [Validators.required, Validators.min(0)]],
    vegetarian: [false],
    available: [true],
  });

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    forkJoin({ categories: this.menuService.getCategories(), items: this.menuService.getItems() }).subscribe({
      next: ({ categories, items }) => {
        this.categories.set(categories);
        this.items.set(items);
      },
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  edit(item: MenuItem): void {
    this.editingId.set(item.id);
    this.form.setValue({
      categoryId: item.categoryId,
      name: item.name,
      description: item.description,
      price: item.price,
      vegetarian: item.vegetarian,
      available: item.available,
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({ categoryId: 0, name: '', description: '', price: 0, vegetarian: false, available: true });
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    const payload = { ...this.form.getRawValue(), imageUrl: null };
    this.isSubmitting.set(true);
    const editingId = this.editingId();
    const request = editingId
      ? this.menuService.updateItem(editingId, payload)
      : this.menuService.createItem(payload);

    request.subscribe({
      next: () => {
        this.toast.success(editingId ? 'Item updated.' : 'Item added.');
        this.cancelEdit();
        this.load();
      },
      error: () => this.isSubmitting.set(false),
      complete: () => this.isSubmitting.set(false),
    });
  }

  toggleAvailability(item: MenuItem): void {
    this.menuService.setAvailability(item.id, !item.available).subscribe({
      next: updated => this.items.update(items => items.map(i => (i.id === updated.id ? updated : i))),
      error: () => {},
    });
  }

  async remove(item: MenuItem): Promise<void> {
    const confirmed = await this.confirmDialog.open(`Delete "${item.name}"? This cannot be undone.`, 'Delete Item');
    if (!confirmed) return;

    this.menuService.deleteItem(item.id).subscribe({
      next: () => {
        this.items.update(items => items.filter(i => i.id !== item.id));
        this.toast.success('Item deleted.');
      },
      error: () => {},
    });
  }

  categoryName(categoryId: number): string {
    return this.categories().find(c => c.id === categoryId)?.name ?? '—';
  }
}
