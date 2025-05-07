import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { TaskManagementService } from '../../../services/services/task-management.service';
import { TaskDto } from '../../../services/models/task-dto';
import {LoadingSpinnerComponent} from "../../../shared/components/loading-spinner/loading-spinner.component";
import {AlertComponent} from "../../../shared/components/alert/alert.component";
import {MainLayoutComponent} from "../../../shared/components/main-layout/main-layout.component";
import {ConfirmDialogComponent} from "../../../shared/components/confirm-dialog/confirm-dialog.component";
import {CommonModule, NgClass} from "@angular/common";
import {SharedModule} from "../../../shared/shared.module";

@Component({
    selector: 'app-task-list',
    templateUrl: './task-list.component.html',
    imports: [
        AlertComponent,
        MainLayoutComponent,
        LoadingSpinnerComponent,
        NgClass,
        SharedModule,
        RouterLink,
        CommonModule
    ],
    styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit {
    tasks: TaskDto[] = [];
    totalItems = 0;
    currentPage = 0;
    pageSize = 10;

    searchForm: FormGroup;
    isLoading = false;
    errorMessage = '';

    showDeleteConfirm = false;
    taskToDelete: TaskDto | null = null;

    constructor(
        private taskService: TaskManagementService,
        private fb: FormBuilder,
        private router: Router
    ) {
        this.searchForm = this.fb.group({
            title: [''],
            status: [''],
            assigneeId: ['']
        });
    }

    ngOnInit(): void {
        this.loadTasks();
    }

    loadTasks(page = 0): void {
        this.isLoading = true;
        this.errorMessage = '';

        const { title, status, assigneeId } = this.searchForm.value;

        this.taskService.getAllTasks({
            title: title || undefined,
            status: status || undefined,
            assigneeId: assigneeId ? Number(assigneeId) : undefined,
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
                this.errorMessage = 'Failed to load tasks';
                this.isLoading = false;
            }
        });
    }

    onSearch(): void {
        this.loadTasks(0); // Reset to first page on search
    }

    resetSearch(): void {
        this.searchForm.reset();
        this.loadTasks(0);
    }

    onPageChange(page: number): void {
        this.loadTasks(page);
    }

    confirmDelete(task: TaskDto): void {
        this.taskToDelete = task;
        this.showDeleteConfirm = true;
    }

    onDeleteConfirmed(): void {
        if (!this.taskToDelete) return;

        this.isLoading = true;
        this.taskService.deleteTask({ id: this.taskToDelete.id as number }).subscribe({
            next: () => {
                // Reload tasks after successful deletion
                this.loadTasks(this.currentPage);
                this.showDeleteConfirm = false;
                this.taskToDelete = null;
            },
            error: (error) => {
                console.error('Error deleting task', error);
                this.errorMessage = 'Failed to delete task';
                this.isLoading = false;
                this.showDeleteConfirm = false;
            }
        });
    }

    onDeleteCancelled(): void {
        this.showDeleteConfirm = false;
        this.taskToDelete = null;
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