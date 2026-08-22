import { Component } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { SessionType } from '../../core/models/order.model';

interface SessionOption {
  value: SessionType;
  label: string;
  emoji: string;
}

const OPTIONS: SessionOption[] = [
  { value: 'BREAKFAST', label: 'Breakfast', emoji: '🍳' },
  { value: 'LUNCH', label: 'Lunch', emoji: '🍽️' },
  { value: 'DINNER', label: 'Dinner', emoji: '🌙' },
];

@Component({
  selector: 'app-session-type-dialog',
  standalone: true,
  imports: [MatDialogModule],
  template: `
    <div class="p-2 sm:p-4 min-w-[280px]">
      <h2 class="text-xl font-bold text-white mb-1">Start Order</h2>
      <p class="text-sm text-gray-400 mb-5">Choose a session type for this table</p>

      <div class="flex flex-col gap-3">
        @for (option of options; track option.value) {
          <button
            type="button"
            class="w-full min-h-tap flex items-center gap-3 rounded-2xl p-3.5 text-left transition-all active:scale-[0.98] hover:border-brand-500/50 hover:bg-white/[0.07]"
            style="background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.1);"
            (click)="dialogRef.close(option.value)"
          >
            <div
              class="w-11 h-11 rounded-xl flex items-center justify-center text-xl flex-shrink-0"
              style="background: rgba(249,115,22,0.15);"
            >
              {{ option.emoji }}
            </div>
            <span class="text-base font-semibold text-white">{{ option.label }}</span>
          </button>
        }
      </div>

      <div class="flex justify-end mt-5">
        <button
          type="button"
          class="min-h-tap px-3 text-sm font-semibold text-gray-400 hover:text-gray-200 transition-colors"
          (click)="dialogRef.close(null)"
        >
          Cancel
        </button>
      </div>
    </div>
  `,
})
export class SessionTypeDialogComponent {
  readonly options = OPTIONS;

  constructor(public dialogRef: MatDialogRef<SessionTypeDialogComponent, SessionType | null>) {}
}
