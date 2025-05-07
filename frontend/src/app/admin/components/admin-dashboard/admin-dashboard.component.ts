import { Component, OnInit } from '@angular/core';
import { TaskManagementService, UserManagementService } from '../../../services/services';
import { TaskDto, UserDto } from '../../../services/models';
import {LoadingSpinnerComponent} from "../../../shared/components/loading-spinner/loading-spinner.component";
import {AlertComponent} from "../../../shared/components/alert/alert.component";
import {CommonModule} from "@angular/common";
import {MainLayoutComponent} from "../../../shared/components/main-layout/main-layout.component";

@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    imports: [
        LoadingSpinnerComponent,
        AlertComponent,
        CommonModule,
        MainLayoutComponent
    ],
    styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
    totalUsers = 0;
    totalTasks = 0;
    pendingTasks = 0;
    completedTasks = 0;
    isLoading = true;
    errorMessage = '';

    recentUsers: UserDto[] = [];
    recentTasks: TaskDto[] = [];

    constructor(
        private userService: UserManagementService,
        private taskService: TaskManagementService
    ) { }

    ngOnInit(): void {
        this.loadDashboardData();
    }

    loadDashboardData(): void {
        this.isLoading = true;

        // Load users data
        this.userService.getAllUsers({
            pageable: { page: 0, size: 5, sort: ['id,desc'] }
        }).subscribe({
            next: (response) => {
                this.totalUsers = response.totalElements || 0;
                this.recentUsers = response.content || [];
            },
            error: (error) => {
                console.error('Error loading users', error);
                this.errorMessage = 'Failed to load users data';
            }
        });

        // Load tasks data
        this.taskService.getAllTasks({
            pageable: { page: 0, size: 5, sort: ['id,desc'] }
        }).subscribe({
            next: (response) => {
                this.totalTasks = response.totalElements || 0;
                this.recentTasks = response.content || [];

                // Calculate task stats
                if (response.content) {
                    this.pendingTasks = response.content.filter(
                        task => task.status === 'TO_DO' || task.status === 'IN_PROGRESS'
                    ).length;
                    this.completedTasks = response.content.filter(
                        task => task.status === 'COMPLETED'
                    ).length;
                }

                this.isLoading = false;
            },
            error: (error) => {
                console.error('Error loading tasks', error);
                this.errorMessage = 'Failed to load tasks data';
                this.isLoading = false;
            }
        });
    }
}