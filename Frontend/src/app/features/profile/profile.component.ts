import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { BookingService } from '../../core/services/booking.service';

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

    this.bookingService.getUserBookings(userId).subscribe({
      next: (bookings) => {
        this.totalBookings.set(bookings.length);

        const now = new Date();
        const upcoming = bookings.filter(b =>
          new Date(b.sessionStartTime) > now && b.status === 'CONFIRMED'
        ).length;
        const completed = bookings.filter(b =>
          b.status === 'COMPLETED'
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
}
