import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-item-notes-dialog',
  standalone: true,
  imports: [FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Item notes</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Notes (optional)</mat-label>
        <textarea matInput rows="3" [(ngModel)]="notes" placeholder="e.g. less spicy, no onion"></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button class="min-h-tap" (click)="dialogRef.close(null)">Cancel</button>
      <button mat-flat-button color="primary" class="min-h-tap" (click)="dialogRef.close(notes)">Save</button>
    </mat-dialog-actions>
  `,
})
export class ItemNotesDialogComponent {
  dialogRef = inject(MatDialogRef<ItemNotesDialogComponent, string | null>);
  notes: string = inject(MAT_DIALOG_DATA) ?? '';
}
