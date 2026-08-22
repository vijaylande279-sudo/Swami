import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { ConfirmDialogComponent, ConfirmDialogData } from './confirm-dialog.component';

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private dialog = inject(MatDialog);

  async open(message: string, title = 'Please confirm', options: Partial<ConfirmDialogData> = {}): Promise<boolean> {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: { title, message, ...options } as ConfirmDialogData,
      disableClose: false,
      autoFocus: false,
    });
    const result = await firstValueFrom(ref.afterClosed());
    return result === true;
  }
}
