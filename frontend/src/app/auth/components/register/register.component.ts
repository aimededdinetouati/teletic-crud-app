import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { AuthService } from '../../services/auth.service';
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterLink
    ],
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {
    registerForm: FormGroup;
    isLoading = false;
    errorMessage = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        // Create registration form with validators
        this.registerForm = this.fb.group({
            fullName: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(3)]]
        });
    }

    onSubmit(): void {
        if (this.registerForm.invalid) {
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';

        this.authService.register(this.registerForm.value).subscribe({
            next: () => {
                this.isLoading = false;
                // Navigation to login is handled in AuthService
            },
            error: (error: { message: string; }) => {
                this.isLoading = false;
                this.errorMessage = error.message || 'Registration failed. Please try again.';
            }
        });
    }
}