export interface TenantResponse {
  id: string;
  name: string;
  slug: string;
  gstin: string | null;
  primaryContactEmail: string;
  primaryContactPhone: string | null;
  status: string;
  trialEndsAt: string | null;
}

export interface InviteResponse {
  id: string;
  email: string;
  roleName: string;
  status: string;
  expiresAt: string;
  inviteToken: string | null;
}

export interface EmployeeResponse {
  userId: string;
  email: string;
  fullName: string | null;
}

export interface RoleResponse {
  id: string;
  tenantId: string | null;
  name: string;
  scope: string;
  system: boolean;
  permissions: string[];
}

export interface PermissionResponse {
  code: string;
  description: string | null;
}
