import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { BookingService } from '../../core/services/booking.service';
import { Booking, BookingStatus } from '../../core/models/booking.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user = signal<any>(null);
  totalBookings = signal(0);
  upcomingBookings = signal(0);
  completedBookings = signal(0);
  isLoading = signal(true);

  constructor(
    private authService: AuthService,
    private bookingService: BookingService
  ) {}

  ngOnInit(): void {
    this.loadUserData();
    this.loadBookingStats();
  }

  loadUserData(): void {
    const currentUser = this.authService.currentUser();
    this.user.set(currentUser);
  }

  loadBookingStats(): void {
    const userId = this.authService.getUserId();
    if (!userId) {
      this.isLoading.set(false);
      return;
    }

    this.bookingService.getMyBookings().subscribe({
      next: (bookings: Booking[]) => {
        this.totalBookings.set(bookings.length);

        const now = new Date();
        const upcoming = bookings.filter((b: Booking) => {
          const start = this.getBookingStartDate(b);
          return !!start && start > now && b.status === BookingStatus.CONFIRMED;
        }).length;
        const completed = bookings.filter((b: Booking) =>
          b.status === BookingStatus.COMPLETED
        ).length;

        this.upcomingBookings.set(upcoming);
        this.completedBookings.set(completed);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  getRoleDisplay(roles: string[]): string {
    if (!roles || roles.length === 0) return 'User';
    return roles.join(', ');
  }

  logout(): void {
    this.authService.logout();
  }

  private getBookingStartDate(booking: Booking): Date | null {
    if (booking.sessionStartTime) {
      return new Date(booking.sessionStartTime);
    }
    if (booking.trainingSession?.date && booking.trainingSession?.startTime) {
      return new Date(`${booking.trainingSession.date}T${booking.trainingSession.startTime}`);
    }
    return null;
  }
}
