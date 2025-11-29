import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  TrainingSession,
  TrainingSessionRequest,
  UpdateSessionRequest
} from '../models/training-session.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TrainingSessionService {
  private readonly API_URL = `${environment.apiUrl}/training-sessions`;

  constructor(private http: HttpClient) {}

  getAllSessions(search?: string): Observable<TrainingSession[]> {
    const params = search ? new HttpParams().set('search', search) : undefined;
    return this.http.get<TrainingSession[]>(this.API_URL, { params });
  }

  searchSessions(term: string): Observable<TrainingSession[]> {
    const params = new HttpParams().set('term', term);
    return this.http.get<TrainingSession[]>(`${this.API_URL}/search`, { params });
  }

  getSessionById(id: number): Observable<TrainingSession> {
    return this.http.get<TrainingSession>(`${this.API_URL}/${id}`);
  }

  getSessionsByTrainer(trainerId: number): Observable<TrainingSession[]> {
    return this.http.get<TrainingSession[]>(`${this.API_URL}/trainer/${trainerId}`);
  }

  getSessionsByDate(date: string): Observable<TrainingSession[]> {
    return this.http.get<TrainingSession[]>(`${this.API_URL}/date/${date}`);
  }

  getSessionsByDateRange(startDate: string, endDate: string): Observable<TrainingSession[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<TrainingSession[]>(`${this.API_URL}/date-range`, { params });
  }

  getUpcomingSessions(): Observable<TrainingSession[]> {
    return this.http.get<TrainingSession[]>(`${this.API_URL}/upcoming`);
  }

  createSession(payload: TrainingSessionRequest): Observable<TrainingSession> {
    return this.http.post<TrainingSession>(this.API_URL, payload);
  }

  updateSession(id: number, payload: UpdateSessionRequest): Observable<TrainingSession> {
    return this.http.put<TrainingSession>(`${this.API_URL}/${id}`, payload);
  }

  cancelSession(id: number): Observable<TrainingSession> {
    return this.http.put<TrainingSession>(`${this.API_URL}/${id}/cancel`, {});
  }

  deleteSession(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
