package com.teletic.test_crud.web.rest

import com.teletic.test_crud.domain.TaskStatus
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.security.AuthoritiesConstants
import com.teletic.test_crud.service.TaskService
import com.teletic.test_crud.service.dto.TaskAssignmentDTO
import com.teletic.test_crud.service.dto.TaskDTO
import com.teletic.test_crud.service.dto.TaskStatusUpdateDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
@Tag(name = "Task Management")
class TaskResource(
    private val taskService: TaskService,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(TaskResource::class.java)

    /**
     * GET /tasks : Get all tasks (admin only)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('${AuthoritiesConstants.ADMIN}')")
    @Operation(summary = "Get all tasks (admin only)")
    fun getAllTasks(
        @Parameter(description = "Filter by title (optional)") @RequestParam(required = false) title: String?,
        @Parameter(description = "Filter by status (optional)") @RequestParam(required = false) status: TaskStatus?,
        @Parameter(description = "Filter by assignee id (optional)") @RequestParam(required = false) assigneeId: Long?,
        @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<TaskDTO>> {
        log.debug("REST request to get all Tasks with filters - title: {}, status: {}, assigneeId: {}", title, status, assigneeId)

        val page = if (title != null || status != null || assigneeId != null) {
            taskService.search(title, status, assigneeId, pageable)
        } else {
            taskService.findAll(pageable)
        }

        return ResponseEntity.ok(page.map { taskService.toDTO(it) })
    }

    /**
     * GET /tasks/my : Get current user's tasks
     */
    @GetMapping("/my")
    @Operation(summary = "Get current user's tasks")
    fun getUserTasks(
        @Parameter(description = "Filter by status (optional)") @RequestParam(required = false) status: TaskStatus?,
        @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<TaskDTO>> {
        // Get current user from security context
        val user = getCurrentUser() ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        log.debug("REST request to get tasks for user {}", user.id)

        val page = when {
            status != null -> taskService.search(null, status, user.id, pageable)
            else -> taskService.findByAssigneeId(user.id!!, pageable)
        }

        return ResponseEntity.ok(page.map { taskService.toDTO(it) })
    }

    /**
     * GET /tasks/:id : Get a task by id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a specific task by ID")
    fun getTask(@PathVariable id: Long): ResponseEntity<TaskDTO> {
        log.debug("REST request to get Task : {}", id)

        // Get current user from security context
        val user = getCurrentUser() ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val task = taskService.findById(id)

        // Check if user is authorized to view this task
        val isAdmin = user.authorities.any { it.authority == AuthoritiesConstants.ADMIN }
        val isAssignee = task.assignee?.id == user.id

        if (!isAdmin && !isAssignee) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(taskService.toDTO(task))
    }

    /**
     * POST /tasks : Create a new task (admin only)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('${AuthoritiesConstants.ADMIN}')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new task (admin only)")
    fun createTask(
        @Valid @RequestBody taskDTO: TaskDTO
    ): ResponseEntity<TaskDTO> {
        log.debug("REST request to save Task : {}", taskDTO)

        if (taskDTO.id != null) {
            return ResponseEntity.badRequest().build()
        }

        val result = taskService.save(taskDTO)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(taskService.toDTO(result))
    }

    /**
     * PUT /tasks/:id : Update a task (admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('${AuthoritiesConstants.ADMIN}')")
    @Operation(summary = "Update an existing task (admin only)")
    fun updateTask(
        @PathVariable id: Long,
        @Valid @RequestBody taskDTO: TaskDTO
    ): ResponseEntity<TaskDTO> {
        log.debug("REST request to update Task : {}", taskDTO)

        if (taskDTO.id == null || taskDTO.id != id) {
            return ResponseEntity.badRequest().build()
        }

        taskService.findById(id)

        val result = taskService.save(taskDTO)
        return ResponseEntity.ok(taskService.toDTO(result))
    }

    /**
     * DELETE /tasks/:id : Delete a task (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('${AuthoritiesConstants.ADMIN}')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task (admin only)")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Task : {}", id)
        taskService.delete(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * PATCH /tasks/:id/assign : Assign a task to a user (admin only)
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('${AuthoritiesConstants.ADMIN}')")
    @Operation(summary = "Assign a task to a user (admin only)")
    fun assignTask(
        @PathVariable id: Long,
        @Valid @RequestBody assignmentDTO: TaskAssignmentDTO
    ): ResponseEntity<TaskDTO> {
        log.debug("REST request to assign Task {} to User {}", id, assignmentDTO.userId)

        val result = taskService.assignToUser(id, assignmentDTO.userId)
        return ResponseEntity.ok(taskService.toDTO(result))
    }

    /**
     * PATCH /tasks/:id/status : Update task status (available to both admin and assigned user)
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status (available to both admin and assigned user)")
    fun updateTaskStatus(
        @PathVariable id: Long,
        @Valid @RequestBody statusDTO: TaskStatusUpdateDTO
    ): ResponseEntity<TaskDTO> {
        log.debug("REST request to update Task {} status to {}", id, statusDTO.status)

        // Get current user from security context
        val user = getCurrentUser() ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // Check if user is authorized to update this task
        val task = taskService.findById(id)
        val isAdmin = user.authorities.any { it.authority == AuthoritiesConstants.ADMIN }
        val isAssignee = task.assignee?.id == user.id

        if (!isAdmin && !isAssignee) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val result = taskService.updateStatus(id, statusDTO.status)
        return ResponseEntity.ok(taskService.toDTO(result))
    }

    /**
     * Helper method to get the current authenticated user
     */
    private fun getCurrentUser(): User? {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated) {
            val principal = auth.principal

            // Check if principal is already a User
            if (principal is User) {
                return principal
            }

            // Principal might be a string representing the username/email
            if (principal is String) {
                return userRepository.findByEmail(principal)
            }

            if (principal is org.springframework.security.core.userdetails.User) {
                return userRepository.findByEmail(principal.username)
            }
        }
        return null
    }
}