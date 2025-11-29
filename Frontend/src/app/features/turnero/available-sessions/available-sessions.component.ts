import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TrainingSessionService } from '../../../core/services/training-session.service';
import { BookingService } from '../../../core/services/booking.service';
import { AuthService } from '../../../core/services/auth.service';
import { TrainingSession } from '../../../core/models/training-session.model';
import { SessionCardComponent } from '../session-card/session-card.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-available-sessions',
  standalone: true,
  imports: [CommonModule, FormsModule, SessionCardComponent, ModalComponent],
  templateUrl: './available-sessions.component.html',
  styleUrls: ['./available-sessions.component.css']
})
export class AvailableSessionsComponent implements OnInit {
  sessions = signal<TrainingSession[]>([]);
  filteredSessions = signal<TrainingSession[]>([]);
  userBookings = signal<Set<number>>(new Set());
  isLoading = signal(true);
  errorMessage = signal('');

  // Modal
  showBookingModal = false;
  selectedSession: TrainingSession | null = null;

  searchTerm = '';

  constructor(
    private sessionService: TrainingSessionService,
    private bookingService: BookingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadSessions();
    this.loadUserBookings();
  }

  loadSessions(): void {
    this.isLoading.set(true);
    this.sessionService.getUpcomingSessions().subscribe({
      next: (sessions) => {
        this.sessions.set(sessions);
        this.applyFilters();
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Failed to load sessions. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  loadUserBookings(): void {
    if (!this.authService.isAuthenticated()) {
      return;
    }

    this.bookingService.getMyUpcomingBookings().subscribe({
      next: (bookings) => {
        const bookedSessionIds = new Set(bookings.map(b => b.trainingSession.id));
        this.userBookings.set(bookedSessionIds);
      },
      error: (error) => {
        console.error('Failed to load user bookings:', error);
      }
    });
  }

  applyFilters(): void {
    let filtered = this.sessions();

    // Search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(s =>
        s.name.toLowerCase().includes(term) ||
        (s.description ?? '').toLowerCase().includes(term) ||
        s.trainer.fullName.toLowerCase().includes(term) ||
        s.location.toLowerCase().includes(term)
      );
    }

    this.filteredSessions.set(filtered);
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  isBooked(sessionId: number): boolean {
    return this.userBookings().has(sessionId);
  }

  onBookSession(sessionId: number): void {
    const session = this.sessions().find(s => s.id === sessionId);
    if (session) {
      this.selectedSession = session;
      this.showBookingModal = true;
    }
  }

  confirmBooking(): void {
    if (!this.selectedSession) return;

    if (!this.authService.isAuthenticated()) {
      this.errorMessage.set('Please login to book a session');
      return;
    }

    this.bookingService.createBooking({ trainingSessionId: this.selectedSession.id }).subscribe({
      next: () => {
        this.showBookingModal = false;
        this.loadSessions();
        this.loadUserBookings();
      },
      error: (error) => {
        this.errorMessage.set(error.message || 'Failed to book session');
        this.showBookingModal = false;
      }
    });
  }

  onCancelBooking(sessionId: number): void {
    if (!this.authService.isAuthenticated()) return;

    this.bookingService.getMyBookings().subscribe({
      next: (bookings) => {
        const booking = bookings.find(
          b => b.trainingSession.id === sessionId && b.status === 'CONFIRMED'
        );
        if (booking) {
          this.bookingService.cancelBooking(booking.id).subscribe({
            next: () => {
              this.loadSessions();
              this.loadUserBookings();
            },
            error: (error) => {
              this.errorMessage.set('Failed to cancel booking');
            }
          });
        }
      },
      error: (error) => {
        this.errorMessage.set('Failed to cancel booking');
      }
    });
  }
}
