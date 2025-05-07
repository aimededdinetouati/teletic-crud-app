// user-task-list.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { TaskManagementService } from '../../../services/services/task-management.service';
import { TaskDto } from '../../../services/models/task-dto';
import {MainLayoutComponent} from "../../../shared/components/main-layout/main-layout.component";
import {SharedModule} from "../../../shared/shared.module";
import {CommonModule, NgClass} from "@angular/common";
import {RouterLink} from "@angular/router";

@Component({
    selector: 'app-user-task-list',
    templateUrl: './user-task-list.component.html',
    imports: [
        MainLayoutComponent,
        SharedModule,
        NgClass,
        RouterLink,
        CommonModule
    ],
    styleUrls: ['./user-task-list.component.css']
})
export class UserTaskListComponent implements OnInit {
    tasks: TaskDto[] = [];
    totalItems = 0;
    currentPage = 0;
    pageSize = 10;

    filterForm: FormGroup;
    isLoading = false;
    errorMessage = '';

    constructor(
        private taskService: TaskManagementService,
        private fb: FormBuilder
    ) {
        this.filterForm = this.fb.group({
            status: ['']
        });
    }

    ngOnInit(): void {
        this.loadUserTasks();
    }

    loadUserTasks(page = 0): void {
        this.isLoading = true;
        this.errorMessage = '';

        const status = this.filterForm.get('status')?.value;

        this.taskService.getUserTasks({
            status: status || undefined,
            pageable: {
                page: page,
                size: this.pageSize,
                sort: ['id,desc']
            }
        }).subscribe({
            next: (response) => {
                this.tasks = response.content || [];
                this.totalItems = response.totalElements || 0;
                this.currentPage = page;
                this.isLoading = false;
            },
            error: (error) => {
                console.error('Error loading tasks', error);
                this.errorMessage = 'Failed to load your tasks';
                this.isLoading = false;
            }
        });
    }

    onFilterChange(): void {
        this.loadUserTasks(0); // Reset to first page when filter changes
    }

    resetFilter(): void {
        this.filterForm.reset();
        this.loadUserTasks(0);
    }

    onPageChange(page: number): void {
        this.loadUserTasks(page);
    }

    // Helper method to format date string
    formatDate(dateString: string | undefined | null): string {
        if (!dateString) return 'Not set';

        const date = new Date(dateString);
        return date.toLocaleDateString();
    }

    // Helper method to get CSS class for task status
    getStatusClass(status: string): string {
        switch (status) {
            case 'TO_DO':
                return 'status-todo';
            case 'IN_PROGRESS':
                return 'status-inprogress';
            case 'COMPLETED':
                return 'status-completed';
            case 'CANCELLED':
                return 'status-cancelled';
            default:
                return '';
        }
    }
}