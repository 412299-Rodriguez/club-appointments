import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Booking, CreateBookingRequest } from '../models/booking.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private readonly API_URL = `${environment.apiUrl}/bookings`;

  constructor(private http: HttpClient) {}

  getAllBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(this.API_URL);
  }

  getBookingById(id: number): Observable<Booking> {
    return this.http.get<Booking>(`${this.API_URL}/${id}`);
  }

  getBookingsByUser(userId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.API_URL}/user/${userId}`);
  }

  getSessionBookings(sessionId: number): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.API_URL}/session/${sessionId}`);
  }

  getMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.API_URL}/my-bookings`);
  }

  getMyUpcomingBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.API_URL}/my-upcoming`);
  }

  createBooking(payload: CreateBookingRequest): Observable<Booking> {
    return this.http.post<Booking>(this.API_URL, payload);
  }

  cancelBooking(id: number): Observable<Booking> {
    return this.http.put<Booking>(`${this.API_URL}/${id}/cancel`, {});
  }

  deleteBooking(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
