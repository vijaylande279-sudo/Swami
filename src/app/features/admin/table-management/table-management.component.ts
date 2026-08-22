import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TableService } from '../../../core/services/table.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmDialogService } from '../../../shared/components/confirm-dialog/confirm-dialog.service';
import { Table } from '../../../core/models/table.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-table-management',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    LoadingSpinnerComponent,
    StatusBadgeComponent,
  ],
  templateUrl: './table-management.component.html',
})
export class TableManagementComponent implements OnInit {
  private fb = inject(FormBuilder);
  private tableService = inject(TableService);
  private toast = inject(ToastService);
  private confirmDialog = inject(ConfirmDialogService);

  readonly isLoading = signal(true);
  readonly isSubmitting = signal(false);
  readonly tables = signal<Table[]>([]);
  readonly editingId = signal<number | null>(null);

  form = this.fb.nonNullable.group({
    tableNumber: ['', Validators.required],
    capacity: [2, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.tableService.getTables().subscribe({
      next: tables => this.tables.set(tables),
      error: () => this.isLoading.set(false),
      complete: () => this.isLoading.set(false),
    });
  }

  edit(table: Table): void {
    this.editingId.set(table.id);
    this.form.setValue({ tableNumber: table.tableNumber, capacity: table.capacity });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({ tableNumber: '', capacity: 2 });
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) return;

    const payload = this.form.getRawValue();
    this.isSubmitting.set(true);
    const editingId = this.editingId();
    const request = editingId
      ? this.tableService.updateTable(editingId, payload)
      : this.tableService.createTable(payload);

    request.subscribe({
      next: () => {
        this.toast.success(editingId ? 'Table updated.' : 'Table added.');
        this.cancelEdit();
        this.load();
      },
      error: () => this.isSubmitting.set(false),
      complete: () => this.isSubmitting.set(false),
    });
  }

  async remove(table: Table): Promise<void> {
    const confirmed = await this.confirmDialog.open(`Delete table "${table.tableNumber}"? This cannot be undone.`, 'Delete Table');
    if (!confirmed) return;

    this.tableService.deleteTable(table.id).subscribe({
      next: () => {
        this.tables.update(tables => tables.filter(t => t.id !== table.id));
        this.toast.success('Table deleted.');
      },
      error: () => {},
    });
  }
}
