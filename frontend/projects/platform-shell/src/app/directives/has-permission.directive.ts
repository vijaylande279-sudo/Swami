import { Directive, EmbeddedViewRef, Input, TemplateRef, ViewContainerRef, effect, inject } from '@angular/core';
import { PermissionService } from '../core/services/permission.service';

/**
 * Structural directive gating a template on the current user's permissions, e.g.
 * `<button *appHasPermission="'tenant:employee:invite'">Invite</button>`.
 * UI-only — assume it can be bypassed; the real enforcement is server-side
 * @PreAuthorize, per PLATFORM_BUILD_INSTRUCTIONS.md §6.2.
 */
@Directive({
  selector: '[appHasPermission]',
  standalone: true,
})
export class HasPermissionDirective {
  private templateRef = inject(TemplateRef<unknown>);
  private viewContainer = inject(ViewContainerRef);
  private permissions = inject(PermissionService);

  private view: EmbeddedViewRef<unknown> | null = null;
  private required: string | string[] = [];

  @Input() set appHasPermission(value: string | string[]) {
    this.required = value;
    this.render();
  }

  constructor() {
    effect(() => {
      this.permissions.permissions();
      this.render();
    });
  }

  private render(): void {
    const required = Array.isArray(this.required) ? this.required : [this.required];
    const allowed = required.length === 0 || this.permissions.hasAny(required);

    if (allowed && !this.view) {
      this.view = this.viewContainer.createEmbeddedView(this.templateRef);
    } else if (!allowed && this.view) {
      this.viewContainer.clear();
      this.view = null;
    }
  }
}
