import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content class="text-base">{{ data.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button class="min-h-tap" (click)="dialogRef.close(false)">
        {{ data.cancelLabel ?? 'Cancel' }}
      </button>
      <button mat-flat-button color="warn" class="min-h-tap" (click)="dialogRef.close(true)">
        {{ data.confirmLabel ?? 'Confirm' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDialogComponent {
  dialogRef = inject(MatDialogRef<ConfirmDialogComponent>);
  data: ConfirmDialogData = inject(MAT_DIALOG_DATA);
}
