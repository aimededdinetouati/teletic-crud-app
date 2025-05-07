// user-task-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskManagementService } from '../../../services/services/task-management.service';
import { TaskDto } from '../../../services/models/task-dto';
import { TaskStatusUpdateDto } from '../../../services/models/task-status-update-dto';
import {SharedModule} from "../../../shared/shared.module";
import {CommonModule, NgClass} from "@angular/common";

@Component({
    selector: 'app-user-task-detail',
    templateUrl: './user-task-detail.component.html',
    imports: [
        SharedModule,
        NgClass,
        CommonModule
    ],
    styleUrls: ['./user-task-detail.component.css']
})
export class UserTaskDetailComponent implements OnInit {
    taskId: number | null = null;
    task: TaskDto | null = null;
    statusForm: FormGroup;

    isLoading = false;
    isUpdating = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private fb: FormBuilder,
        private taskService: TaskManagementService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.statusForm = this.fb.group({
            status: ['', Validators.required]
        });
    }

    ngOnInit(): void {
        // Get task ID from route
        const idParam = this.route.snapshot.paramMap.get('id');

        if (idParam) {
            this.taskId = Number(idParam);
            this.loadTaskDetails();
        } else {
            this.errorMessage = 'No task ID provided';
        }
    }

    loadTaskDetails(): void {
        if (!this.taskId) return;

        this.isLoading = true;

        this.taskService.getTask({ id: this.taskId }).subscribe({
            next: (task) => {
                this.task = task;

                // Set current status in form
                this.statusForm.patchValue({
                    status: task.status
                });

                this.isLoading = false;
            },
            error: (error) => {
                console.error('Error loading task details', error);
                this.errorMessage = 'Failed to load task details';
                this.isLoading = false;
            }
        });
    }

    updateStatus(): void {
        if (this.statusForm.invalid || !this.taskId) {
            return;
        }

        const newStatus = this.statusForm.value.status as TaskStatusUpdateDto['status'];

        this.isUpdating = true;
        this.errorMessage = '';
        this.successMessage = '';

        this.taskService.updateTaskStatus({
            id: this.taskId,
            body: { status: newStatus }
        }).subscribe({
            next: (updatedTask) => {
                this.task = updatedTask;
                this.isUpdating = false;
                this.successMessage = 'Task status updated successfully';
            },
            error: (error) => {
                console.error('Error updating task status', error);
                this.errorMessage = error.message || 'Failed to update task status';
                this.isUpdating = false;
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
    getStatusClass(status: string | undefined | null): string {
        if (status == null) return '';
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