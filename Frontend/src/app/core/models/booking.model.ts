import { User } from './user.model';
import { TrainingSession } from './training-session.model';

export interface Booking {
  id: number;
  user: User;
  trainingSession: TrainingSession;
  status: BookingStatus;
  createdAt?: string;
  updatedAt?: string;
}

export enum BookingStatus {
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED'
}

export interface CreateBookingRequest {
  trainingSessionId: number;
}
