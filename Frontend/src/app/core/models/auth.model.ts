import { UserRole } from './user.model';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  type: string;
  userId: number;
  email: string;
  fullName: string;
  role: UserRole;
  expiresIn: number;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  fullName: string;
  phone?: string;
}

export type RegisterResponse = AuthResponse;

export interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  refreshToken: string | null;
  user: {
    id: number;
    email: string;
    fullName: string;
    role: UserRole;
  } | null;
}
