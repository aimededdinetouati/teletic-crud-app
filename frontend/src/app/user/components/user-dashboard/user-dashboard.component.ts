import { Component, OnInit } from '@angular/core';
import { TaskManagementService } from '../../../services/services/task-management.service';
import { TaskDto } from '../../../services/models/task-dto';
import { AuthService } from '../../../auth/services/auth.service';
import {RouterLink} from "@angular/router";
import {CommonModule, NgClass} from "@angular/common";
import {LoadingSpinnerComponent} from "../../../shared/components/loading-spinner/loading-spinner.component";
import {AlertComponent} from "../../../shared/components/alert/alert.component";
import {MainLayoutComponent} from "../../../shared/components/main-layout/main-layout.component";

@Component({
    selector: 'app-user-dashboard',
    templateUrl: './user-dashboard.component.html',
    imports: [
        RouterLink,
        NgClass,
        LoadingSpinnerComponent,
        AlertComponent,
        MainLayoutComponent,
        CommonModule
    ],
    styleUrls: ['./user-dashboard.component.css']
})
export class UserDashboardComponent implements OnInit {
    tasks: TaskDto[] = [];
    totalTasks = 0;
    todoTasks = 0;
    inProgressTasks = 0;
    completedTasks = 0;

    isLoading = true;
    errorMessage = '';
    currentUser: any;

    constructor(
        private taskService: TaskManagementService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        this.loadUserTasks();
    }

    loadUserTasks(): void {
        this.isLoading = true;

        this.taskService.getUserTasks({
            pageable: { page: 0, size: 10, sort: ['id,desc'] }
        }).subscribe({
            next: (response) => {
                this.tasks = response.content || [];
                this.totalTasks = response.totalElements || 0;

                // Calculate task stats
                this.todoTasks = this.tasks.filter(task => task.status === 'TO_DO').length;
                this.inProgressTasks = this.tasks.filter(task => task.status === 'IN_PROGRESS').length;
                this.completedTasks = this.tasks.filter(task => task.status === 'COMPLETED').length;

                this.isLoading = false;
            },
            error: (error) => {
                console.error('Error loading tasks', error);
                this.errorMessage = 'Failed to load your tasks';
                this.isLoading = false;
            }
        });
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