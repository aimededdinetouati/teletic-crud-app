import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { AuthenticationService } from '../../services/services/authentication.service';
import { AuthenticationRequestDto, RegistrationRequestDto } from '../../services/models';

interface AuthResponse {
    token: string;
    userId: number;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private currentUserSubject = new BehaviorSubject<any>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    redirectUrl: string = '/';

    constructor(
        private router: Router,
        private apiAuthService: AuthenticationService
    ) {
        // Check for existing token on startup
        this.loadUserFromStorage();
    }

    private loadUserFromStorage(): void {
        const tokenData = localStorage.getItem('auth_token');
        const userData = localStorage.getItem('user_data');

        if (tokenData && userData) {
            try {
                const user = JSON.parse(userData);
                this.currentUserSubject.next(user);
            } catch (error) {
                // Handle invalid storage data
                this.logout();
            }
        }
    }

    login(email: string, password: string): Observable<any> {
        return this.apiAuthService.login({ body: { email, password } })
            .pipe(
                tap((response: any) => {
                    // Save token and user data
                    localStorage.setItem('auth_token', response.token);

                    // Save user data (including role information)
                    const userData = {
                        id: response.userId,
                        email: email,
                        roles: this.extractRolesFromToken(response.token)
                    };

                    localStorage.setItem('user_data', JSON.stringify(userData));
                    this.currentUserSubject.next(userData);

                    // Navigate to appropriate dashboard
                    if (this.isAdmin()) {
                        this.router.navigate(['/admin/dashboard']);
                    } else {
                        this.router.navigate(['/user/dashboard']);
                    }
                }),
                catchError(this.handleError)
            );
    }

    register(user: RegistrationRequestDto): Observable<any> {
        return this.apiAuthService.register({ body: user })
            .pipe(
                tap(() => {
                    // After registration, navigate to login
                    this.router.navigate(['/auth/login']);
                }),
                catchError(this.handleError)
            );
    }

    logout(): void {
        // Clear stored data
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user_data');

        // Update current user
        this.currentUserSubject.next(null);

        // Navigate to login
        this.router.navigate(['/auth/login']);
    }

    isLoggedIn(): boolean {
        return !!localStorage.getItem('auth_token');
    }

    isAdmin(): boolean {
        const userData = localStorage.getItem('user_data');
        if (!userData) return false;

        try {
            const user = JSON.parse(userData);
            return user.roles && user.roles.includes('ROLE_ADMIN');
        } catch (error) {
            return false;
        }
    }

    getCurrentUser(): any {
        return this.currentUserSubject.value;
    }

    getAuthToken(): string | null {
        return localStorage.getItem('auth_token');
    }

    // Helper method to extract roles from JWT token
    private extractRolesFromToken(token: string): string[] {
        try {
            // JWT tokens consist of three parts separated by dots
            const parts = token.split('.');
            if (parts.length !== 3) return [];

            // Get the payload part (middle) and decode it
            const payload = JSON.parse(atob(parts[1]));

            // Extract roles from payload (adjust based on your token structure)
            return payload.authorites || [];
        } catch (error) {
            console.error('Error parsing JWT token', error);
            return [];
        }
    }

    private handleError(error: HttpErrorResponse) {
        let errorMessage = 'An unknown error occurred';

        if (error.error instanceof ErrorEvent) {
            // Client-side error
            errorMessage = `Error: ${error.error.message}`;
        } else {
            // Server-side error
            if (error.status === 401) {
                errorMessage = 'Invalid email or password';
            } else if (error.error && error.error.message) {
                errorMessage = error.error.message;
            } else {
                errorMessage = `Error Code: ${error.status}, Message: ${error.message}`;
            }
        }

        return throwError(() => new Error(errorMessage));
    }
}