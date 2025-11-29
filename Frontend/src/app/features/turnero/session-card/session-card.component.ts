import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrainingSession } from '../../../core/models/training-session.model';

@Component({
  selector: 'app-session-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './session-card.component.html',
  styleUrls: ['./session-card.component.css']
})
export class SessionCardComponent {
  @Input() session!: TrainingSession;
  @Input() isBooked = false;
  @Output() bookSession = new EventEmitter<number>();
  @Output() cancelBooking = new EventEmitter<number>();

  onBook(): void {
    this.bookSession.emit(this.session.id);
  }

  onCancel(): void {
    this.cancelBooking.emit(this.session.id);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric'
    });
  }

  formatTime(time: string): string {
    return time;
  }

  getStatusClass(): string {
    switch (this.session.status) {
      case 'CANCELLED':
        return 'bg-red-600';
      case 'COMPLETED':
        return 'bg-blue-600';
      default:
        return 'bg-accent-blue';
    }
  }

  availableSpots(): number {
    return Math.max(this.session.maxParticipants - this.session.currentParticipants, 0);
  }

  isFull(): boolean {
    return this.availableSpots() <= 0;
  }

  isAlmostFull(): boolean {
    const remaining = this.availableSpots();
    const percentage = remaining / this.session.maxParticipants;
    return percentage <= 0.3 && percentage > 0;
  }
}
