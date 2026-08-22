export type UserRole = 'ADMIN' | 'WAITER' | 'KITCHEN';

export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  id: number;
  name: string;
  email: string;
  role: UserRole;
}
