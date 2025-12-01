import { User } from './user.model';
import { TrainingSession } from './training-session.model';

export interface Booking {
  id: number;
  user: User;
  trainingSession: TrainingSession;
  status: BookingStatus;
  sessionStartTime?: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum BookingStatus {
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

export interface CreateBookingRequest {
  trainingSessionId: number;
}
