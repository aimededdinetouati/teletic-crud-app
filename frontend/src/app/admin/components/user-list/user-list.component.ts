import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { UserManagementService } from '../../../services/services/user-management.service';
import { UserDto } from '../../../services/models/user-dto';
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-user-list',
    templateUrl: './user-list.component.html',
    styleUrls: ['./user-list.component.css'],
    imports: [
        CommonModule,
    ]
})
export class UserListComponent implements OnInit {
    users: UserDto[] = [];
    totalItems = 0;
    currentPage = 0;
    pageSize = 10;

    searchForm: FormGroup;
    isLoading = false;
    errorMessage = '';

    showDeleteConfirm = false;
    userToDelete: UserDto | null = null;

    constructor(
        private userService: UserManagementService,
        private fb: FormBuilder,
        private router: Router
    ) {
        this.searchForm = this.fb.group({
            fullName: [''],
            email: [''],
            role: ['']
        });
    }

    ngOnInit(): void {
        this.loadUsers();
    }

    loadUsers(page = 0): void {
        this.isLoading = true;
        this.errorMessage = '';

        const { fullName, email, role } = this.searchForm.value;

        this.userService.getAllUsers({
            fullName: fullName || undefined,
            email: email || undefined,
            role: role || undefined,
            pageable: {
                page: page,
                size: this.pageSize,
                sort: ['id,desc']
            }
        }).subscribe({
            next: (response) => {
                this.users = response.content || [];
                this.totalItems = response.totalElements || 0;
                this.currentPage = page;
                this.isLoading = false;
            },
            error: (error) => {
                console.error('Error loading users', error);
                this.errorMessage = 'Failed to load users';
                this.isLoading = false;
            }
        });
    }

    onSearch(): void {
        this.loadUsers(0); // Reset to first page on search
    }

    resetSearch(): void {
        this.searchForm.reset();
        this.loadUsers(0);
    }

    onPageChange(page: number): void {
        this.loadUsers(page);
    }

    confirmDelete(user: UserDto): void {
        this.userToDelete = user;
        this.showDeleteConfirm = true;
    }

    onDeleteConfirmed(): void {
        if (!this.userToDelete) return;

        this.isLoading = true;
        this.userService.deleteUser({ userId: this.userToDelete.id }).subscribe({
            next: () => {
                // Reload users after successful deletion
                this.loadUsers(this.currentPage);
                this.showDeleteConfirm = false;
                this.userToDelete = null;
            },
            error: (error) => {
                console.error('Error deleting user', error);
                this.errorMessage = 'Failed to delete user';
                this.isLoading = false;
                this.showDeleteConfirm = false;
            }
        });
    }

    onDeleteCancelled(): void {
        this.showDeleteConfirm = false;
        this.userToDelete = null;
    }
}