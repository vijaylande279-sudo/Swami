import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Client } from '@stomp/stompjs';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SocketService {
  private platformId = inject(PLATFORM_ID);
  private client: Client | null = null;

  readonly connected = signal(false);

  async connect(token: string): Promise<void> {
    if (!isPlatformBrowser(this.platformId) || this.client) return;

    // sockjs-client is a CommonJS package that touches the Node `global` object at module
    // load time. A dynamic import keeps it out of the eagerly-evaluated bootstrap graph
    // (and out of the SSR bundle entirely), so it only ever loads in the browser.
    const { default: SockJS } = await import('sockjs-client');

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${environment.wsUrl}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => this.connected.set(true),
      onWebSocketClose: () => this.connected.set(false),
      onStompError: () => this.connected.set(false),
    });
    this.client.activate();
  }

  subscribe<T>(topic: string): Observable<T> {
    return new Observable<T>(observer => {
      if (!this.client) {
        observer.complete();
        return;
      }
      const sub = this.client.subscribe(topic, msg => {
        observer.next(JSON.parse(msg.body) as T);
      });
      return () => sub.unsubscribe();
    });
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
    this.connected.set(false);
  }
}
