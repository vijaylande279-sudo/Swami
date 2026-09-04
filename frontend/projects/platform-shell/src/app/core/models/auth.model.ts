export interface LoginResponse {
  mfaRequired: boolean;
  mfaChallengeId: string | null;
  accessToken: string | null;
  refreshToken: string | null;
}

export interface TokenPairResponse {
  accessToken: string;
  refreshToken: string;
}

export interface MeResponse {
  id: string;
  tenantId: string | null;
  email: string;
  fullName: string | null;
  roles: string[];
  permissions: string[];
  mfaEnabled: boolean;
}

export interface JwtClaims {
  sub: string;
  tenant_id?: string;
  roles?: string[];
  perms?: string[];
  entitlements?: string[];
  exp: number;
}

export interface MfaEnrollResponse {
  secret: string;
  otpAuthUri: string;
}

export interface MfaVerifyResponse {
  backupCodes: string[];
}
