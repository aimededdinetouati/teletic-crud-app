import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { AuthService } from '../../services/auth.service';
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css'],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterLink
    ],
})
export class LoginComponent implements OnInit {
    loginForm: FormGroup;
    isLoading = false;
    errorMessage = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        // Create login form with validators
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(3)]]
        });
    }

    ngOnInit(): void {
        // Redirect if already logged in
        if (this.authService.isLoggedIn()) {
            if (this.authService.isAdmin()) {
                this.router.navigate(['/admin/dashboard']);
            } else {
                this.router.navigate(['/user/dashboard']);
            }
        }
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';

        const { email, password } = this.loginForm.value;

        this.authService.login(email, password).subscribe({
            next: () => {
                this.isLoading = false;
                // Navigation is handled in AuthService
            },
            error: (error: { message: string; }) => {
                this.isLoading = false;
                this.errorMessage = error.message || 'Login failed. Please try again.';
            }
        });
    }
}