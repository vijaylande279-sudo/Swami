# 🏨 Swami Hotel — OMS Frontend Guide
### Angular 17+ | Tailwind CSS | Angular Material | STOMP WebSocket

> Read this before writing any code. Kitchen staff NEVER sees the tables page.

---

## 📐 Tech Stack

| Layer | Choice |
|---|---|
| Framework | Angular 17+ standalone components |
| Language | TypeScript 5.x strict ON |
| Styling | Tailwind CSS v3 + Angular Material |
| State | Angular Signals |
| HTTP | Angular `HttpClient` + interceptors |
| Real-time | `@stomp/stompjs` + `sockjs-client` |
| Forms | Angular Reactive Forms |
| Print | `window.print()` + `@media print` |
| QR | `<img>` base64 from backend |

---

## 🗂️ Project Structure

```
src/app/
├── core/
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── socket.service.ts
│   │   └── toast.service.ts
│   ├── interceptors/
│   │   ├── auth.interceptor.ts
│   │   └── error.interceptor.ts
│   ├── guards/
│   │   ├── auth.guard.ts
│   │   └── role.guard.ts
│   └── models/
│       ├── user.model.ts
│       ├── table.model.ts
│       ├── menu.model.ts
│       ├── order.model.ts
│       └── bill.model.ts
├── features/
│   ├── auth/
│   │   └── login/
│   │       └── login.component.ts    ← New modern design
│   ├── tables/
│   │   └── tables.component.ts       ← Waiter + Admin only
│   ├── menu/
│   │   └── menu.component.ts
│   ├── kitchen/
│   │   └── kitchen.component.ts      ← KITCHEN ONLY — order cards
│   ├── bill/
│   │   └── bill.component.ts
│   └── admin/
│       └── admin.component.ts
├── shared/
│   ├── components/
│   │   ├── navbar/navbar.component.ts
│   │   └── order-ready-notification/
└── app.routes.ts
```

---

## 🔐 Role-based Routing — CRITICAL

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: 'login',   loadComponent: () => import('./features/auth/login/login.component') },
  { path: 'tables',  loadComponent: () => import('./features/tables/tables.component'),
    canActivate: [authGuard, roleGuard(['ADMIN','WAITER'])] },
  { path: 'menu/:tableId', loadComponent: () => import('./features/menu/menu.component'),
    canActivate: [authGuard, roleGuard(['ADMIN','WAITER'])] },
  { path: 'kitchen', loadComponent: () => import('./features/kitchen/kitchen.component'),
    canActivate: [authGuard, roleGuard(['KITCHEN','ADMIN'])] },
  { path: 'bill/:orderId', loadComponent: () => import('./features/bill/bill.component'),
    canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: 'admin',   loadComponent: () => import('./features/admin/admin.component'),
    canActivate: [authGuard, roleGuard(['ADMIN'])] },
  { path: '', redirectTo: 'tables', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
```

### Redirect after login — by role

```typescript
// auth.service.ts — call this after successful login
redirectAfterLogin(role: string): void {
  switch (role) {
    case 'KITCHEN': this.router.navigate(['/kitchen']); break;
    case 'ADMIN':   this.router.navigate(['/admin']);   break;
    default:        this.router.navigate(['/tables']);  break; // WAITER
  }
}
```

> ⚠️ KITCHEN role MUST go to `/kitchen`. If they land on `/tables` the routing is broken.

---

## 🎨 Design System — Swami Hotel

### Color tokens (tailwind.config.js)
```js
colors: {
  brand: {
    50:  '#fff7ed',
    100: '#ffedd5',
    400: '#fb923c',
    500: '#f97316',
    600: '#ea580c',
    700: '#c2410c',
    900: '#7c2d12',
  },
  kitchen: {
    bg:     '#0f0f0f',
    card:   '#1a1a1a',
    border: '#2a2a2a',
  }
}
```

### Page themes
| Page | Theme | Reason |
|---|---|---|
| Login | Warm gradient, split layout | First impression |
| Tables | Light, card grid | Quick scanning |
| Menu | Light, category tabs | Browsing |
| Kitchen | **Dark (#0f0f0f)** | Screen in bright kitchen |
| Bill | White receipt | Print ready |
| Admin | Light, dashboard | Data overview |

---

## 🔑 Login Page — New Design

```typescript
// login.component.ts
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatIconModule],
  template: `
  <div class="min-h-screen flex">

    <!-- Left panel — branding -->
    <div class="hidden lg:flex lg:w-1/2 relative overflow-hidden
                bg-gradient-to-br from-orange-600 via-orange-500 to-red-600
                flex-col items-center justify-center p-12">

      <!-- Background pattern -->
      <div class="absolute inset-0 opacity-10"
           style="background-image: radial-gradient(circle at 2px 2px, white 1px, transparent 0);
                  background-size: 32px 32px;">
      </div>

      <div class="relative z-10 text-center text-white">
        <div class="text-8xl mb-6">🏨</div>
        <h1 class="text-5xl font-black tracking-tight mb-3">
          SWAMI HOTEL
        </h1>
        <p class="text-orange-100 text-xl font-light mb-8">
          Order Management System
        </p>
        <div class="flex gap-6 justify-center text-sm text-orange-200">
          <div class="flex flex-col items-center gap-1">
            <span class="text-2xl">🍽️</span>
            <span>Fine Dining</span>
          </div>
          <div class="flex flex-col items-center gap-1">
            <span class="text-2xl">⚡</span>
            <span>Fast Orders</span>
          </div>
          <div class="flex flex-col items-center gap-1">
            <span class="text-2xl">📱</span>
            <span>Mobile Ready</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right panel — login form -->
    <div class="flex-1 flex items-center justify-center p-8 bg-gray-50">
      <div class="w-full max-w-md">

        <!-- Mobile logo -->
        <div class="lg:hidden text-center mb-8">
          <span class="text-5xl">🏨</span>
          <h1 class="text-3xl font-black text-orange-600 mt-2">SWAMI HOTEL</h1>
        </div>

        <!-- Form card -->
        <div class="bg-white rounded-3xl shadow-xl p-8 border border-gray-100">

          <h2 class="text-2xl font-bold text-gray-900 mb-1">Welcome back</h2>
          <p class="text-gray-500 text-sm mb-8">Sign in to your account</p>

          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-5">

            <!-- Email -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Email address
              </label>
              <div class="relative">
                <mat-icon class="absolute left-3 top-1/2 -translate-y-1/2
                                 text-gray-400 text-xl">mail</mat-icon>
                <input formControlName="email" type="email"
                       placeholder="you@swamihotel.com"
                       class="w-full pl-10 pr-4 py-3 border border-gray-200
                              rounded-xl text-gray-900 text-sm
                              focus:outline-none focus:ring-2 focus:ring-orange-500
                              focus:border-transparent transition-all
                              placeholder:text-gray-400">
              </div>
              <p *ngIf="form.get('email')?.invalid && form.get('email')?.touched"
                 class="text-red-500 text-xs mt-1">
                Enter a valid email
              </p>
            </div>

            <!-- Password -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Password
              </label>
              <div class="relative">
                <mat-icon class="absolute left-3 top-1/2 -translate-y-1/2
                                 text-gray-400 text-xl">lock</mat-icon>
                <input formControlName="password"
                       [type]="showPassword ? 'text' : 'password'"
                       placeholder="••••••••"
                       class="w-full pl-10 pr-12 py-3 border border-gray-200
                              rounded-xl text-gray-900 text-sm
                              focus:outline-none focus:ring-2 focus:ring-orange-500
                              focus:border-transparent transition-all">
                <button type="button"
                        (click)="showPassword = !showPassword"
                        class="absolute right-3 top-1/2 -translate-y-1/2
                               text-gray-400 hover:text-gray-600">
                  <mat-icon class="text-xl">
                    {{ showPassword ? 'visibility_off' : 'visibility' }}
                  </mat-icon>
                </button>
              </div>
            </div>

            <!-- Error message -->
            <div *ngIf="errorMessage"
                 class="bg-red-50 border border-red-200 rounded-xl
                        p-3 flex items-center gap-2">
              <mat-icon class="text-red-500 text-lg">error</mat-icon>
              <p class="text-red-600 text-sm">{{ errorMessage }}</p>
            </div>

            <!-- Submit -->
            <button type="submit"
                    [disabled]="form.invalid || isLoading"
                    class="w-full bg-gradient-to-r from-orange-500 to-red-500
                           hover:from-orange-600 hover:to-red-600
                           text-white font-bold py-3.5 rounded-xl
                           transition-all duration-200 flex items-center
                           justify-center gap-2 disabled:opacity-60
                           disabled:cursor-not-allowed shadow-lg
                           shadow-orange-200">
              <span *ngIf="isLoading"
                    class="w-4 h-4 border-2 border-white border-t-transparent
                           rounded-full animate-spin">
              </span>
              {{ isLoading ? 'Signing in...' : 'Sign in' }}
            </button>

          </form>

          <!-- Role hint -->
          <div class="mt-6 pt-6 border-t border-gray-100">
            <p class="text-xs text-gray-400 text-center mb-3">
              Access by role
            </p>
            <div class="flex gap-2 justify-center">
              <span class="px-3 py-1 bg-purple-100 text-purple-700
                           rounded-full text-xs font-semibold">Admin</span>
              <span class="px-3 py-1 bg-blue-100 text-blue-700
                           rounded-full text-xs font-semibold">Waiter</span>
              <span class="px-3 py-1 bg-orange-100 text-orange-700
                           rounded-full text-xs font-semibold">Kitchen</span>
            </div>
          </div>

        </div>
      </div>
    </div>

  </div>
  `
})
export class LoginComponent {
  form = inject(FormBuilder).nonNullable.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(4)]],
  });

  showPassword = false;
  isLoading    = false;
  errorMessage = '';

  private auth   = inject(AuthService);
  private router = inject(Router);

  onSubmit(): void {
    if (this.form.invalid) return;
    this.isLoading    = true;
    this.errorMessage = '';

    this.auth.login(this.form.getRawValue()).subscribe({
      next: (res) => {
        this.auth.redirectAfterLogin(res.data.role);
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Invalid email or password';
        this.isLoading = false;
      }
    });
  }
}
```

---

## 🍳 Kitchen Component — ONLY FOR KITCHEN ROLE

> This is a completely separate component from tables. Kitchen staff NEVER sees tables.

```typescript
// features/kitchen/kitchen.component.ts
@Component({
  selector: 'app-kitchen',
  standalone: true,
  imports: [CommonModule, MatIconModule, DatePipe],
  template: `
  <div class="min-h-screen" style="background:#0f0f0f">

    <!-- Kitchen Navbar — dark theme -->
    <nav style="background:#1a1a1a; border-bottom:1px solid #2a2a2a"
         class="sticky top-0 z-50 px-6 py-4 flex items-center justify-between">

      <div class="flex items-center gap-3">
        <div class="w-2 h-2 rounded-full bg-green-400 animate-pulse"></div>
        <span class="text-gray-400 text-sm">Live Orders</span>
        <span class="bg-orange-500 text-white text-xs font-bold
                     px-2 py-0.5 rounded-full">
          {{ orders.length }}
        </span>
      </div>

      <h1 class="text-xl font-black"
          style="background: linear-gradient(135deg, #f97316, #ef4444);
                 -webkit-background-clip: text;
                 -webkit-text-fill-color: transparent">
        🏨 SWAMI HOTEL
      </h1>

      <div class="flex items-center gap-3">
        <span class="px-3 py-1 rounded-full text-xs font-bold
                     bg-orange-500/20 text-orange-400 border border-orange-500/30">
          KITCHEN
        </span>
        <button (click)="logout()"
                class="text-gray-500 hover:text-red-400 transition-colors p-1">
          <mat-icon>logout</mat-icon>
        </button>
      </div>
    </nav>

    <!-- New order alert -->
    <div *ngIf="newOrderAlert"
         class="mx-6 mt-4 p-4 rounded-2xl border border-orange-500/50
                flex items-center gap-4 animate-pulse"
         style="background: rgba(249,115,22,0.15)">
      <span class="text-3xl">🔔</span>
      <div>
        <p class="text-orange-400 font-bold text-lg">
          New Order — Table {{ newOrderAlert.tableNumber }}!
        </p>
        <p class="text-orange-300/70 text-sm">
          {{ newOrderAlert.items?.length }} item(s) waiting
        </p>
      </div>
      <button (click)="newOrderAlert = null"
              class="ml-auto text-gray-500 hover:text-white">
        <mat-icon>close</mat-icon>
      </button>
    </div>

    <!-- Section header -->
    <div class="px-6 pt-6 pb-4">
      <h2 class="text-white text-lg font-bold">Active Orders</h2>
      <p class="text-gray-500 text-sm">
        {{ orders.length === 0 ? 'All caught up!' : orders.length + ' orders in queue' }}
      </p>
    </div>

    <!-- Orders grid -->
    <div class="px-6 pb-8 grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">

      <div *ngFor="let order of orders; trackBy: trackById"
           class="rounded-2xl overflow-hidden transition-all duration-300"
           [style.border]="getBorderStyle(order.status)"
           style="background:#1a1a1a">

        <!-- Card header -->
        <div class="px-5 pt-5 pb-3 flex items-start justify-between"
             [style.background]="getHeaderBg(order.status)">
          <div>
            <p class="text-gray-400 text-xs font-medium uppercase tracking-wider mb-1">
              {{ order.sessionType }} SESSION
            </p>
            <h3 class="text-white text-3xl font-black">
              Table {{ order.tableNumber }}
            </h3>
          </div>
          <div class="flex flex-col items-end gap-2">
            <span class="text-xs font-bold px-3 py-1 rounded-full"
                  [style.background]="getStatusBg(order.status)"
                  [style.color]="getStatusColor(order.status)">
              {{ getStatusLabel(order.status) }}
            </span>
            <span class="text-gray-500 text-xs">
              {{ order.createdAt | date:'hh:mm a' }}
            </span>
          </div>
        </div>

        <!-- Order items -->
        <div class="px-5 py-4 space-y-3 border-b" style="border-color:#2a2a2a">
          <div *ngFor="let item of order.items"
               class="flex items-start justify-between gap-3">
            <div class="flex-1 min-w-0">
              <p class="text-white font-semibold text-sm truncate">
                {{ item.name }}
              </p>
              <p *ngIf="item.notes"
                 class="text-yellow-400 text-xs mt-0.5 flex items-center gap-1">
                <mat-icon class="text-xs" style="font-size:12px">edit_note</mat-icon>
                {{ item.notes }}
              </p>
            </div>
            <span class="flex-shrink-0 w-8 h-8 rounded-full
                         bg-orange-500 text-white text-sm font-black
                         flex items-center justify-center">
              {{ item.quantity }}
            </span>
          </div>
        </div>

        <!-- Order notes -->
        <div *ngIf="order.notes"
             class="px-5 py-3 border-b flex items-start gap-2"
             style="background:rgba(234,179,8,0.08); border-color:#2a2a2a">
          <mat-icon class="text-yellow-400 text-sm flex-shrink-0"
                    style="font-size:16px;margin-top:2px">sticky_note_2</mat-icon>
          <p class="text-yellow-300 text-sm">{{ order.notes }}</p>
        </div>

        <!-- Action buttons -->
        <div class="px-5 py-4">

          <!-- Start Cooking button -->
          <button *ngIf="order.status === 'SENT_TO_KITCHEN'"
                  (click)="markPreparing(order.id)"
                  class="w-full py-3.5 rounded-xl font-bold text-sm
                         transition-all duration-200 flex items-center
                         justify-center gap-2 text-white"
                  style="background: linear-gradient(135deg, #2563eb, #1d4ed8)">
            <mat-icon style="font-size:18px">soup_kitchen</mat-icon>
            Start Cooking
          </button>

          <!-- Order Complete button -->
          <button *ngIf="order.status === 'PREPARING'"
                  (click)="markReady(order.id)"
                  class="w-full py-3.5 rounded-xl font-bold text-sm
                         transition-all duration-200 flex items-center
                         justify-center gap-2 text-white"
                  style="background: linear-gradient(135deg, #16a34a, #15803d)">
            <mat-icon style="font-size:18px">check_circle</mat-icon>
            Order Complete — Notify Waiter
          </button>

          <!-- Waiting state -->
          <div *ngIf="order.status === 'READY'"
               class="w-full py-3.5 rounded-xl text-sm font-bold
                      flex items-center justify-center gap-2
                      border border-green-500/40"
               style="background:rgba(22,163,74,0.12); color:#4ade80">
            <mat-icon style="font-size:18px">hourglass_top</mat-icon>
            Ready — Waiter notified
          </div>

        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div *ngIf="orders.length === 0"
         class="flex flex-col items-center justify-center py-32 px-4">
      <div class="text-8xl mb-6 opacity-30">🍳</div>
      <h3 class="text-gray-500 text-xl font-bold mb-2">No active orders</h3>
      <p class="text-gray-600 text-sm text-center">
        Orders from waiters will appear here automatically
      </p>
    </div>

  </div>
  `
})
export class KitchenComponent implements OnInit, OnDestroy {
  orders       = signal<any[]>([]);
  newOrderAlert = signal<any>(null);
  currentUser  = inject(AuthService).user;

  private http    = inject(HttpClient);
  private socket  = inject(SocketService);
  private auth    = inject(AuthService);
  private router  = inject(Router);
  private subs    = new Subscription();
  private audio   = new Audio('/assets/sounds/ding.mp3');

  ngOnInit(): void {
    this.loadOrders();

    this.subs.add(
      this.socket.kitchen$.subscribe(notification => {
        this.newOrderAlert.set(notification);
        this.audio.play().catch(() => {});
        this.loadOrders();
        setTimeout(() => this.newOrderAlert.set(null), 8000);
      })
    );
  }

  loadOrders(): void {
    this.http.get<any>(`${environment.apiUrl}/api/orders/kitchen`)
      .subscribe(res => { if (res.success) this.orders.set(res.data); });
  }

  markPreparing(id: number): void {
    this.http.patch(`${environment.apiUrl}/api/orders/${id}/preparing`, {})
      .subscribe(() => this.loadOrders());
  }

  markReady(id: number): void {
    this.http.patch(`${environment.apiUrl}/api/orders/${id}/ready`, {})
      .subscribe(() => this.loadOrders());
  }

  logout(): void { this.auth.logout(); }

  trackById = (_: number, o: any) => o.id;

  getBorderStyle(status: string): string {
    const map: Record<string, string> = {
      SENT_TO_KITCHEN: '2px solid #f97316',
      PREPARING:       '2px solid #3b82f6',
      READY:           '2px solid #22c55e',
    };
    return map[status] ?? '2px solid #2a2a2a';
  }

  getHeaderBg(status: string): string {
    const map: Record<string, string> = {
      SENT_TO_KITCHEN: 'rgba(249,115,22,0.08)',
      PREPARING:       'rgba(59,130,246,0.08)',
      READY:           'rgba(34,197,94,0.08)',
    };
    return map[status] ?? 'transparent';
  }

  getStatusBg(s: string) {
    return { SENT_TO_KITCHEN:'rgba(249,115,22,0.2)', PREPARING:'rgba(59,130,246,0.2)', READY:'rgba(34,197,94,0.2)' }[s] ?? '#374151';
  }
  getStatusColor(s: string) {
    return { SENT_TO_KITCHEN:'#fb923c', PREPARING:'#60a5fa', READY:'#4ade80' }[s] ?? '#9ca3af';
  }
  getStatusLabel(s: string) {
    return { SENT_TO_KITCHEN:'New Order', PREPARING:'Cooking...', READY:'Ready' }[s] ?? s;
  }

  ngOnDestroy(): void { this.subs.unsubscribe(); }
}
```

---

## 🏷️ Navbar Component — Design Rules

### Layout (3 zones)

```
LEFT                          RIGHT
────────────────────────────────────────────────────────
🏨  SWAMI HOTEL               [Avatar] Vijay Lande  [⎋]
    Order Management                   WAITER
```

### Navbar background
Dark navy gradient — `linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)`
Height: `64px`. Box shadow: `0 2px 20px rgba(0,0,0,0.3)`. Position: `sticky top-0 z-50`.

### Left — Brand block
- Orange square icon box (`36×36px`, `border-radius: 10px`, gradient `#f97316 → #ef4444`) with 🏨 emoji
- Hotel name: `SWAMI HOTEL` — white, `font-weight: 900`, `letter-spacing: 1.5px`, `font-size: 17px`
- Subtitle below name: `Order Management` — `rgba(255,255,255,0.4)`, `9px`, `letter-spacing: 2px`, uppercase

### Right — User block
- Avatar circle (`34×34px`, `border-radius: 50%`, gradient `#f97316 → #ef4444`, white border `2px rgba(255,255,255,0.2)`) showing first letter of name
- User name: white, `13px`, `font-weight: 600`
- User role below name: `rgba(255,255,255,0.4)`, `10px`
- Thin vertical divider: `1px`, `rgba(255,255,255,0.12)`, `28px` tall
- Logout button: `32×32px`, `border-radius: 8px`, transparent bg, `rgba(255,255,255,0.12)` border — on hover `rgba(239,68,68,0.15)` bg + `#ef4444` icon color

### Strict rules
- ❌ No role badge anywhere in navbar — removed completely
- ❌ No "Reconnecting..." text — ever, anywhere in the app
- ❌ No white/light navbar background — always dark navy gradient
- ❌ Hotel name is NOT centered — it is on the LEFT with the brand icon
- ❌ No user name on the left side
- ✅ Kitchen page uses its own separate dark navbar — does NOT use this component
- ✅ All text in navbar is white or white with reduced opacity — never dark text on dark bg

---

## 🟢 Table Cards — Green Border on Order Ready

```html
<div *ngFor="let table of tables()"
     class="rounded-2xl p-5 cursor-pointer transition-all duration-300
            border-2 hover:shadow-lg"
     [ngClass]="{
       'border-green-500 bg-green-50':   table.orderReady,
       'border-orange-300 bg-orange-50': table.status === 'OCCUPIED',
       'border-gray-200 bg-white':       table.status === 'AVAILABLE',
       'border-yellow-300 bg-yellow-50': table.status === 'RESERVED'
     }"
     (click)="selectTable(table)">

  <!-- Green pulse indicator when order is ready -->
  <div *ngIf="table.orderReady"
       class="flex items-center gap-2 mb-3 px-3 py-1.5
              bg-green-500 rounded-full w-fit">
    <span class="w-2 h-2 bg-white rounded-full animate-pulse"></span>
    <span class="text-white text-xs font-bold">Order Ready to Serve!</span>
  </div>

  <h3 class="text-2xl font-black text-gray-800">{{ table.tableNumber }}</h3>
  <p class="text-gray-400 text-sm mb-3">{{ table.capacity }} seats</p>

  <span class="px-3 py-1 rounded-full text-xs font-bold"
        [ngClass]="{
          'bg-green-100 text-green-700':  table.status === 'AVAILABLE',
          'bg-orange-100 text-orange-700':table.status === 'OCCUPIED',
          'bg-yellow-100 text-yellow-700':table.status === 'RESERVED'
        }">
    {{ table.status }}
  </span>
</div>
```

---

## 🧾 Bill — Print + QR Code

```html
<div id="printable-bill">
  <!-- Header -->
  <div style="text-align:center;margin-bottom:16px">
    <h1 style="font-size:22px;font-weight:900;color:#ea580c">🏨 SWAMI HOTEL</h1>
    <p style="font-size:11px;color:#6b7280">Fine Dining Experience</p>
    <hr style="margin:10px 0;border-color:#e5e7eb">
  </div>

  <!-- Items -->
  <div *ngFor="let item of bill?.items"
       style="display:flex;justify-content:space-between;
              font-size:12px;padding:5px 0;border-bottom:1px solid #f3f4f6">
    <span>{{ item.name }} × {{ item.quantity }}</span>
    <span style="font-weight:700">₹{{ item.lineTotal }}</span>
  </div>

  <!-- Totals -->
  <div style="margin-top:12px;font-size:12px">
    <div style="display:flex;justify-content:space-between;padding:3px 0">
      <span>Subtotal</span><span>₹{{ bill?.subtotal }}</span>
    </div>
    <div style="display:flex;justify-content:space-between;padding:3px 0">
      <span>GST ({{ bill?.taxPercent }}%)</span><span>₹{{ bill?.taxAmount }}</span>
    </div>
    <hr style="margin:8px 0">
    <div style="display:flex;justify-content:space-between;
                font-size:17px;font-weight:900;color:#ea580c">
      <span>TOTAL</span><span>₹{{ bill?.total }}</span>
    </div>
  </div>

  <!-- QR Code -->
  <div style="text-align:center;margin-top:16px">
    <p style="font-size:12px;font-weight:700;margin-bottom:8px">
      📱 Scan to Pay via UPI
    </p>
    <img [src]="'data:image/png;base64,' + bill?.qrCodeBase64"
         style="width:160px;height:160px;margin:0 auto">
    <p style="font-size:10px;color:#6b7280;margin-top:4px">
      swamihotel&#64;upi
    </p>
  </div>

  <div style="text-align:center;margin-top:16px;font-size:10px;color:#9ca3af">
    <p>Thank you for dining at Swami Hotel 🙏</p>
  </div>
</div>
```

---

## 🖨️ Print CSS (src/styles/print.scss)

```scss
@media print {
  body * { visibility: hidden; }
  #printable-bill, #printable-bill * { visibility: visible; }
  #printable-bill {
    position: absolute; top: 0; left: 0;
    width: 300px; font-family: 'Courier New', monospace; padding: 16px;
  }
  .no-print, app-navbar, app-order-ready-notification { display: none !important; }
}
```

---

## 🔔 Order Ready Notification

```typescript
// Shown on waiter/admin pages as floating toast
template: `
<div class="fixed top-20 right-4 z-50 space-y-3 w-80">
  <div *ngFor="let n of notifications"
       class="bg-white border-l-4 border-green-500 rounded-2xl
              shadow-2xl p-4 flex items-start gap-3">
    <div class="w-10 h-10 rounded-full bg-green-100 flex items-center
                justify-center flex-shrink-0">
      <mat-icon class="text-green-600">check_circle</mat-icon>
    </div>
    <div class="flex-1">
      <p class="font-bold text-gray-900 text-sm">Order Ready! 🎉</p>
      <p class="text-gray-500 text-xs mt-0.5">
        Table {{ n.tableNumber }} — Please serve now
      </p>
    </div>
    <button (click)="dismiss(n)" class="text-gray-300 hover:text-gray-500">
      <mat-icon style="font-size:16px">close</mat-icon>
    </button>
  </div>
</div>
`
```

---

## 🚫 Never Do These

| ❌ | ✅ |
|---|---|
| Kitchen sees `/tables` | Kitchen redirects to `/kitchen` |
| "Reconnecting..." anywhere in UI | Remove completely — never allowed |
| Role badge in navbar | No badge at all — completely removed |
| Any text or badge on LEFT of navbar | Left is brand only (icon + hotel name) |
| Hotel name centered in navbar | Hotel name on the LEFT with icon box |
| White or light navbar background | Dark navy gradient always |
| Dark text on navbar | White or `rgba(255,255,255,opacity)` only |
| Tailwind classes in `#printable-bill` | Inline styles + `print.scss` only |
| JWT in `localStorage` | Memory in `AuthService` only |
| Bill total calculated in Angular | Always fetch from server |
| Kitchen uses light/white navbar | Kitchen has its own separate dark navbar |
| Skip empty state on any page | Always show a meaningful empty state |
| Forget `ngOnDestroy` unsubscribe | Always clean up socket subscriptions |

---

## 📋 Pre-commit Checklist

- [ ] KITCHEN login → `/kitchen` page — never `/tables`
- [ ] Kitchen page is dark theme (`#0f0f0f`) with order cards
- [ ] Each order card has "Start Cooking" → "Order Complete" buttons
- [ ] Navbar background is dark navy gradient — not white
- [ ] Brand icon + "SWAMI HOTEL" on the LEFT of navbar
- [ ] No role badge anywhere in navbar
- [ ] Logged-in user avatar (initial) + name + role on RIGHT of navbar
- [ ] Logout button has red hover state
- [ ] No "Reconnecting..." text anywhere in the app
- [ ] Table cards show green border + pulse badge when order is READY
- [ ] Bill page shows QR code from backend base64
- [ ] Bill prints cleanly — navbar and buttons hidden in print
- [ ] Login page has split layout (brand gradient left, form right)
- [ ] All pages work on 375px mobile screen
- [ ] `ng build --configuration=production` passes with zero errors

---
*Swami Hotel OMS — updated 2026-08-18*
