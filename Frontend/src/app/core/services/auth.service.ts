import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import { StorageService } from './storage.service';
import { environment } from '../../../environments/environment';
import { UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;

  // Using Angular 19 signals
  isAuthenticated = signal<boolean>(false);
  currentUser = signal<any>(null);

  constructor(
    private http: HttpClient,
    private storageService: StorageService,
    private router: Router
  ) {
    this.checkAuthState();
  }

  private checkAuthState(): void {
    const token = this.storageService.getToken();
    const user = this.storageService.getUser();

    if (token && user) {
      this.isAuthenticated.set(true);
      this.currentUser.set(user);
    }
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => this.setSession(response))
    );
  }

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData).pipe(
      tap(response => this.setSession(response))
    );
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.storageService.getRefreshToken();
    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken }).pipe(
      tap(response => this.setSession(response))
    );
  }

  logout(): void {
    this.http.post(`${this.API_URL}/logout`, {}).subscribe({
      next: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  isAdmin(): boolean {
    return this.currentUser()?.role === UserRole.SUPER_ADMIN;
  }

  isCoach(): boolean {
    return this.currentUser()?.role === UserRole.ENTRENADOR;
  }

  getUserId(): number | null {
    return this.currentUser()?.id ?? null;
  }

  getUserEmail(): string | null {
    return this.currentUser()?.email ?? null;
  }

  private setSession(response: AuthResponse): void {
    this.storageService.saveToken(response.token);
    this.storageService.saveRefreshToken(response.refreshToken);
    this.storageService.saveUser({
      id: response.userId,
      email: response.email,
      fullName: response.fullName,
      role: response.role
    });
    this.isAuthenticated.set(true);
    this.currentUser.set(this.storageService.getUser());
  }

  private clearSession(): void {
    this.storageService.clear();
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }
}
