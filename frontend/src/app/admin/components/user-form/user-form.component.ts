import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserManagementService } from '../../../services/services/user-management.service';
import { UserDto } from '../../../services/models/user-dto';
import {AlertComponent} from "../../../shared/components/alert/alert.component";
import {LoadingSpinnerComponent} from "../../../shared/components/loading-spinner/loading-spinner.component";
import {CommonModule, NgClass} from "@angular/common";
import {MainLayoutComponent} from "../../../shared/components/main-layout/main-layout.component";
import {SharedModule} from "../../../shared/shared.module";

@Component({
    selector: 'app-user-form',
    templateUrl: './user-form.component.html',
    imports: [
        AlertComponent,
        MainLayoutComponent,
        LoadingSpinnerComponent,
        NgClass,
        SharedModule,
        CommonModule
    ],
    styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnInit {
    userForm: FormGroup;
    userId: number | null = null;
    isEditing = false;
    isLoading = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private fb: FormBuilder,
        private userService: UserManagementService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        // Initialize the form with validators
        this.userForm = this.fb.group({
            fullName: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(3)]],
            isAdmin: [false]
        });
    }

    ngOnInit(): void {
        // Check if we're editing an existing user
        const idParam = this.route.snapshot.paramMap.get('id');

        if (idParam) {
            this.userId = Number(idParam);
            this.isEditing = true;
            this.loadUserDetails();

            // Password is optional when editing
            this.userForm.get('password')?.clearValidators();
            this.userForm.get('password')?.setValidators(Validators.minLength(3));
            this.userForm.get('password')?.updateValueAndValidity();
        }
    }

    loadUserDetails(): void {
        if (!this.userId) return;

        this.isLoading = true;

        this.userService.getUser({ userId: this.userId }).subscribe({
            next: (user) => {
                // Populate form with user data
                this.userForm.patchValue({
                    fullName: user.fullName,
                    email: user.email,
                    // Note: We don't set the password as it's not returned by the API
                    isAdmin: user.roles.includes('ROLE_ADMIN')
                });

                this.isLoading = false;
            },
            error: (error) => {
                console.error('Error loading user details', error);
                this.errorMessage = 'Failed to load user details';
                this.isLoading = false;
            }
        });
    }

    onSubmit(): void {
        if (this.userForm.invalid) {
            // Mark all fields as touched to show validation errors
            Object.keys(this.userForm.controls).forEach(key => {
                this.userForm.get(key)?.markAsTouched();
            });
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        const formData = this.userForm.value;

        // Prepare the request data
        const userData = {
            fullName: formData.fullName,
            email: formData.email,
            password: formData.password || ''  // Empty string if not provided during edit
        };

        if (this.isEditing && this.userId) {
            // Update existing user
            this.userService.updateUser({
                userId: this.userId,
                isAdmin: formData.isAdmin,
                body: userData
            }).subscribe({
                next: (response) => {
                    this.isLoading = false;
                    this.successMessage = 'User updated successfully';

                    // Navigate back to user list after a short delay
                    setTimeout(() => {
                        this.router.navigate(['/admin/users']);
                    }, 1500);
                },
                error: (error) => {
                    console.error('Error updating user', error);
                    this.errorMessage = error.message || 'Failed to update user';
                    this.isLoading = false;
                }
            });
        } else {
            // Create new user
            this.userService.createUser({
                isAdmin: formData.isAdmin,
                body: userData
            }).subscribe({
                next: () => {
                    this.isLoading = false;
                    this.successMessage = 'User created successfully';

                    // Navigate back to user list after a short delay
                    setTimeout(() => {
                        this.router.navigate(['/admin/users']);
                    }, 1500);
                },
                error: (error) => {
                    console.error('Error creating user', error);
                    this.errorMessage = error.message || 'Failed to create user';
                    this.isLoading = false;
                }
            });
        }
    }
}