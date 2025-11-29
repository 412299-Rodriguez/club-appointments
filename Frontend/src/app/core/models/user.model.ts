export interface User {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  createdAt?: string;
  updatedAt?: string;
}

export enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',
  ENTRENADOR = 'ENTRENADOR',
  USUARIO = 'USUARIO'
}

export type UserProfile = User;
