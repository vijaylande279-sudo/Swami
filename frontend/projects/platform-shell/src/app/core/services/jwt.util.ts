import { JwtClaims } from '../models/auth.model';

/** Decodes a JWT's payload without verifying the signature (verification is the server's job) — used only to read claims for UI display/permission checks. */
export function decodeJwtClaims(token: string): JwtClaims | null {
  const parts = token.split('.');
  if (parts.length !== 3) {
    return null;
  }
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    return JSON.parse(atob(padded)) as JwtClaims;
  } catch {
    return null;
  }
}
