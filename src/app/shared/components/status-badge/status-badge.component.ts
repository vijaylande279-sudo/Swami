import { Component, Input, computed, signal } from '@angular/core';

const STATUS_STYLES: Record<string, string> = {
  AVAILABLE: 'bg-green-500/15 text-green-400',
  OCCUPIED: 'bg-red-500/15 text-red-400',
  RESERVED: 'bg-amber-500/15 text-amber-400',
  ORDER_READY: 'bg-green-500 text-white',
  READY_FOR_BILL: 'bg-purple-500 text-white',
  OPEN: 'bg-blue-500/15 text-blue-400',
  SENT_TO_KITCHEN: 'bg-amber-500/15 text-amber-400',
  SERVED: 'bg-gray-500/20 text-gray-300',
  BILLED: 'bg-purple-500/15 text-purple-400',
  CLOSED: 'bg-gray-500/20 text-gray-300',
  PENDING: 'bg-blue-500/15 text-blue-400',
  PREPARING: 'bg-amber-500/15 text-amber-400',
  READY: 'bg-green-500/15 text-green-400',
};

@Component({
  selector: 'app-status-badge',
  standalone: true,
  template: `
    <span
      class="inline-flex items-center rounded-full px-3 py-1 text-sm font-medium whitespace-nowrap"
      [class]="classes()"
    >
      {{ label() }}
    </span>
  `,
})
export class StatusBadgeComponent {
  private statusSig = signal<string>('');

  @Input()
  set status(value: string) {
    this.statusSig.set(value ?? '');
  }
  get status(): string {
    return this.statusSig();
  }

  readonly classes = computed(() => STATUS_STYLES[this.statusSig()] ?? 'bg-gray-500/20 text-gray-300');
  readonly label = computed(() =>
    this.statusSig()
      .split('_')
      .map(w => w.charAt(0) + w.slice(1).toLowerCase())
      .join(' '),
  );
}
