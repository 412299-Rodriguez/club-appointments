import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookingService } from '../../../core/services/booking.service';
import { AuthService } from '../../../core/services/auth.service';
import { Booking } from '../../../core/models/booking.model';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, ModalComponent],
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.css']
})
export class MyBookingsComponent implements OnInit {
  bookings = signal<Booking[]>([]);
  upcomingBookings = signal<Booking[]>([]);
  pastBookings = signal<Booking[]>([]);
  isLoading = signal(true);
  errorMessage = signal('');

  // Modal
  showCancelModal = false;
  selectedBooking: Booking | null = null;

  // Tab
  activeTab = 'upcoming';

  constructor(
    private bookingService: BookingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    if (!this.authService.isAuthenticated()) {
      this.errorMessage.set('Please login to view your bookings');
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.bookingService.getMyBookings().subscribe({
      next: (bookings) => {
        this.bookings.set(bookings);
        this.categorizeBookings(bookings);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Failed to load bookings. Please try again.');
        this.isLoading.set(false);
      }
    });
  }

  categorizeBookings(bookings: Booking[]): void {
    const now = new Date();
    const upcoming = bookings.filter(b => {
      const sessionStart = new Date(`${b.trainingSession.date}T${b.trainingSession.startTime}`);
      return sessionStart > now && b.status === 'CONFIRMED';
    });
    const past = bookings.filter(b => {
      const sessionStart = new Date(`${b.trainingSession.date}T${b.trainingSession.startTime}`);
      return sessionStart <= now || b.status === 'CANCELLED';
    });

    this.upcomingBookings.set(upcoming);
    this.pastBookings.set(past);
  }

  onCancelBooking(booking: Booking): void {
    this.selectedBooking = booking;
    this.showCancelModal = true;
  }

  confirmCancellation(): void {
    if (!this.selectedBooking) return;

    this.bookingService.cancelBooking(this.selectedBooking.id).subscribe({
      next: () => {
        this.showCancelModal = false;
        this.selectedBooking = null;
        this.loadBookings();
      },
      error: (error) => {
        this.errorMessage.set('Failed to cancel booking');
        this.showCancelModal = false;
      }
    });
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  formatTime(time: string): string {
    return time;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'CONFIRMED':
        return 'bg-green-500';
      case 'CANCELLED':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  }

  canCancelBooking(booking: Booking): boolean {
    const sessionStart = new Date(`${booking.trainingSession.date}T${booking.trainingSession.startTime}`);
    const now = new Date();
    const hoursDifference = (sessionStart.getTime() - now.getTime()) / (1000 * 60 * 60);

    return booking.status === 'CONFIRMED' && hoursDifference > 2;
  }
}
