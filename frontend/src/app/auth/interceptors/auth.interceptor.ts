import { Injectable } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private authService: AuthService, private router: Router) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Skip token for auth endpoints
        if (request.url.includes('/auth/login') || request.url.includes('/auth/register')) {
            return next.handle(request);
        }

        // Add auth token to other requests
        const token = this.authService.getAuthToken();

        if (token) {
            // Clone the request and add token
            const authReq = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });

            // Handle 401 Unauthorized responses (token expired)
            return next.handle(authReq).pipe(
                catchError((error: HttpErrorResponse) => {
                    if (error.status === 401) {
                        // Token expired or invalid
                        this.authService.logout();
                        this.router.navigate(['/auth/login']);
                    }
                    return throwError(() => error);
                })
            );
        }

        return next.handle(request);
    }
}