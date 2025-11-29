import { User } from './user.model';

export interface TrainingSession {
  id: number;
  name: string;
  description?: string;
  trainer: User;
  date: string;
  startTime: string;
  endTime: string;
  location: string;
  maxParticipants: number;
  currentParticipants: number;
  status: SessionStatus;
  slotConfigurationId?: number;
  deleted?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export enum SessionStatus {
  ACTIVE = 'ACTIVE',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

export interface TrainingSessionRequest {
  name: string;
  description?: string;
  trainerId: number;
  date: string;
  startTime: string;
  endTime: string;
  location: string;
  maxParticipants: number;
  slotConfigId?: number;
}

export type UpdateSessionRequest = Partial<TrainingSessionRequest>;
