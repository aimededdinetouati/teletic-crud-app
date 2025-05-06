package com.teletic.test_crud.service.dto

import com.teletic.test_crud.domain.TaskStatus
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

/**
 * DTO for creating or updating a task
 */
data class TaskDTO(
    val id: Long? = null,

    @field:NotBlank(message = "Title is required")
    @field:Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    val title: String,

    val description: String? = null,

    @field:NotNull(message = "Status is required")
    val status: TaskStatus,

    @field:FutureOrPresent(message = "Due date must be in the present or future")
    val dueDate: Instant? = null,

    val assigneeId: Long? = null,

    val createdDate: Instant? = null,

    val lastModifiedDate: Instant? = null
)

/**
 * DTO for task status update
 */
data class TaskStatusUpdateDTO(
    @field:NotNull(message = "Status is required")
    val status: TaskStatus
)

/**
 * DTO for task assignment
 */
data class TaskAssignmentDTO(
    @field:NotNull(message = "User ID is required")
    val userId: Long
)