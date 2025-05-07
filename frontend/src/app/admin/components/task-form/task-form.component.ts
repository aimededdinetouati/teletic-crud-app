import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskManagementService } from '../../../services/services/task-management.service';
import { TaskDto } from '../../../services/models/task-dto';
import {AlertComponent} from "../../../shared/components/alert/alert.component";
import {LoadingSpinnerComponent} from "../../../shared/components/loading-spinner/loading-spinner.component";
import {CommonModule, NgClass} from "@angular/common";
import {MainLayoutComponent} from "../../../shared/components/main-layout/main-layout.component";
import {SharedModule} from "../../../shared/shared.module";

@Component({
    selector: 'app-task-form',
    templateUrl: './task-form.component.html',
    imports: [
        AlertComponent,
        MainLayoutComponent,
        LoadingSpinnerComponent,
        NgClass,
        SharedModule,
        CommonModule
    ],
    styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
    taskForm: FormGroup;
    taskId: number | null = null;
    isEditing = false;
    isLoading = false;
    errorMessage = '';
    successMessage = '';

    constructor(
        private fb: FormBuilder,
        private taskService: TaskManagementService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        // Initialize the form with validators
        this.taskForm = this.fb.group({
            title: ['', [Validators.required, Validators.minLength(3)]],
            description: [''],
            status: ['TO_DO', Validators.required],
            dueDate: [''],
            assigneeId: ['']
        });
    }

    ngOnInit(): void {
        // Check if we're editing an existing task
        const idParam = this.route.snapshot.paramMap.get('id');

        if (idParam) {
            this.taskId = Number(idParam);
            this.isEditing = true;
            this.loadTaskDetails();
        }
    }

    loadTaskDetails(): void {
        if (!this.taskId) return;

        this.isLoading = true;

        this.taskService.getTask({ id: this.taskId }).subscribe({
            next: (task) => {
                // Format date for form input (YYYY-MM-DD)
                let formattedDueDate = '';
                if (task.dueDate) {
                    const date = new Date(task.dueDate);
                    formattedDueDate = date.toISOString().split('T')[0];
                }

                // Populate form with task data
                this.taskForm.patchValue({
                    title: task.title,
                    description: task.description || '',
                    status: task.status,
                    dueDate: formattedDueDate,
                    assigneeId: task.assigneeId || ''
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

    onSubmit(): void {
        if (this.taskForm.invalid) {
            // Mark all fields as touched to show validation errors
            Object.keys(this.taskForm.controls).forEach(key => {
                this.taskForm.get(key)?.markAsTouched();
            });
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        const formData = this.taskForm.value;

        // Convert date string to ISO format if present
        let dueDate = null;
        if (formData.dueDate) {
            dueDate = new Date(formData.dueDate).toISOString();
        }

        // Prepare the request data
        const taskData: TaskDto = {
            title: formData.title,
            description: formData.description || '',
            status: formData.status,
            dueDate: dueDate,
            assigneeId: formData.assigneeId ? Number(formData.assigneeId) : undefined
        };

        // Add ID if editing
        if (this.isEditing && this.taskId) {
            taskData.id = this.taskId;
        }

        if (this.isEditing && this.taskId) {
            // Update existing task
            this.taskService.updateTask({
                id: this.taskId,
                body: taskData
            }).subscribe({
                next: (response) => {
                    this.isLoading = false;
                    this.successMessage = 'Task updated successfully';

                    // Navigate back to task list after a short delay
                    setTimeout(() => {
                        this.router.navigate(['/admin/tasks']);
                    }, 1500);
                },
                error: (error) => {
                    console.error('Error updating task', error);
                    this.errorMessage = error.message || 'Failed to update task';
                    this.isLoading = false;
                }
            });
        } else {
            // Create new task
            this.taskService.createTask({
                body: taskData
            }).subscribe({
                next: () => {
                    this.isLoading = false;
                    this.successMessage = 'Task created successfully';

                    // Navigate back to task list after a short delay
                    setTimeout(() => {
                        this.router.navigate(['/admin/tasks']);
                    }, 1500);
                },
                error: (error) => {
                    console.error('Error creating task', error);
                    this.errorMessage = error.message || 'Failed to create task';
                    this.isLoading = false;
                }
            });
        }
    }
}