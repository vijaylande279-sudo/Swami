import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { OrderReadyNotifierComponent } from './shared/components/order-ready-notifier/order-ready-notifier.component';
import { AdminNotifierComponent } from './shared/components/admin-notifier/admin-notifier.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, OrderReadyNotifierComponent, AdminNotifierComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
