// task-assign.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskManagementService } from '../../../services/services/task-management.service';
import { UserManagementService } from '../../../services/services/user-management.service';
import { TaskDto } from '../../../services/models/task-dto';
import { UserDto } from '../../../services/models/user-dto';
import {AlertComponent} from "../../../shared/components/alert/alert.component";
import {MainLayoutComponent} from "../../../shared/components/main-layout/main-layout.component";
import {LoadingSpinnerComponent} from "../../../shared/components/loading-spinner/loading-spinner.component";
import {NgClass} from "@angular/common";
import {SharedModule} from "../../../shared/shared.module";

@Component({
    selector: 'app-task-assign',
    templateUrl: './task-assign.component.html',
    imports: [
        AlertComponent,
        MainLayoutComponent,
        LoadingSpinnerComponent,
        NgClass,
        SharedModule
    ],
    styleUrls: ['./task-assign.component.css']
})
export class TaskAssignComponent implements OnInit {
    assignForm: FormGroup;
    taskId: number | null = null;
    task: TaskDto | null = null;
    users: UserDto[] = [];

    isLoadingTask = false;
    isLoadingUsers = false;
    isSubmitting = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private fb: FormBuilder,
        private taskService: TaskManagementService,
        private userService: UserManagementService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.assignForm = this.fb.group({
            userId: ['', Validators.required]
        });
    }

    ngOnInit(): void {
        // Get task ID from route
        const idParam = this.route.snapshot.paramMap.get('id');

        if (idParam) {
            this.taskId = Number(idParam);
            this.loadTaskDetails();
            this.loadUsers();
        } else {
            this.errorMessage = 'No task ID provided';
        }
    }

    loadTaskDetails(): void {
        if (!this.taskId) return;

        this.isLoadingTask = true;

        this.taskService.getTask({ id: this.taskId }).subscribe({
            next: (task) => {
                this.task = task;

                // If task already has an assignee, pre-select in form
                if (task.assigneeId) {
                    this.assignForm.patchValue({
                        userId: task.assigneeId
                    });
                }

                this.isLoadingTask = false;
            },
            error: (error) => {
                console.error('Error loading task details', error);
                this.errorMessage = 'Failed to load task details';
                this.isLoadingTask = false;
            }
        });
    }

    loadUsers(): void {
        this.isLoadingUsers = true;

        this.userService.getAllUsers({
            pageable: { page: 0, size: 100 }  // Load all users for the dropdown
        }).subscribe({
            next: (response) => {
                this.users = response.content || [];
                this.isLoadingUsers = false;
            },
            error: (error) => {
                console.error('Error loading users', error);
                this.errorMessage = 'Failed to load users';
                this.isLoadingUsers = false;
            }
        });
    }

    onSubmit(): void {
        if (this.assignForm.invalid || !this.taskId) {
            return;
        }

        const userId = Number(this.assignForm.value.userId);

        this.isSubmitting = true;
        this.errorMessage = '';
        this.successMessage = '';

        this.taskService.assignTask({
            id: this.taskId,
            body: { userId }
        }).subscribe({
            next: (response) => {
                this.isSubmitting = false;
                this.successMessage = 'Task assigned successfully';

                // Navigate back to task list after a short delay
                setTimeout(() => {
                    this.router.navigate(['/admin/tasks']);
                }, 1500);
            },
            error: (error) => {
                console.error('Error assigning task', error);
                this.errorMessage = error.message || 'Failed to assign task';
                this.isSubmitting = false;
            }
        });
    }
}