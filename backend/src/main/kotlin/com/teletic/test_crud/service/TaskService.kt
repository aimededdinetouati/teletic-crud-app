package com.teletic.test_crud.service

import com.teletic.test_crud.domain.Task
import com.teletic.test_crud.domain.TaskStatus
import com.teletic.test_crud.repository.TaskRepository
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.service.dto.TaskDTO
import com.teletic.test_crud.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(TaskService::class.java)

    /**
     * Save a task
     */
    @Transactional
    fun save(taskDTO: TaskDTO): Task {
        log.debug("Request to save Task : {}", taskDTO)

        val task = Task(
            id = taskDTO.id,
            title = taskDTO.title,
            description = taskDTO.description,
            status = taskDTO.status,
            dueDate = taskDTO.dueDate,
            assignee = taskDTO.assigneeId?.let {
                userRepository.findById(it).orElseThrow {
                    BadRequestAlertException("User not found", "task", "userNotFound")
                }
            }
        )
        return taskRepository.save(task)
    }

    /**
     * Get all tasks
     */
    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<Task> {
        log.debug("Request to get all Tasks")
        return taskRepository.findAll(pageable)
    }

    /**
     * Get tasks by search criteria
     */
    @Transactional(readOnly = true)
    fun search(title: String?, status: TaskStatus?, assigneeId: Long?, pageable: Pageable): Page<Task> {
        log.debug("Request to search Tasks with criteria - title: {}, status: {}, assigneeId: {}", title, status, assigneeId)
        return taskRepository.search(
            title?.takeIf { it.isNotBlank() },
            status,
            assigneeId,
            pageable
        )
    }

    /**
     * Get tasks assigned to a specific user
     */
    @Transactional(readOnly = true)
    fun findByAssigneeId(userId: Long, pageable: Pageable): Page<Task> {
        log.debug("Request to get Tasks assigned to user: {}", userId)
        return taskRepository.findByAssigneeId(userId, pageable)
    }

    /**
     * Get one task by id
     */
    @Transactional(readOnly = true)
    fun findById(id: Long): Task {
        log.debug("Request to get Task : {}", id)
        return taskRepository.findById(id)
            .orElseThrow { BadRequestAlertException("Task not found", "task", "idnotfound") }
    }

    /**
     * Delete task by id
     */
    @Transactional
    fun delete(id: Long) {
        log.debug("Request to delete Task : {}", id)
        val task = findById(id)
        taskRepository.delete(task)
    }

    /**
     * Assign task to user
     */
    @Transactional
    fun assignToUser(taskId: Long, userId: Long): Task {
        log.debug("Request to assign Task {} to User {}", taskId, userId)

        val task = findById(taskId)
        val user = userRepository.findById(userId)
            .orElseThrow { BadRequestAlertException("User not found", "task", "userNotFound") }

        task.assignee = user
        return taskRepository.save(task)
    }

    /**
     * Update task status
     */
    @Transactional
    fun updateStatus(taskId: Long, status: TaskStatus): Task {
        log.debug("Request to update Task {} status to {}", taskId, status)

        val task = findById(taskId)
        task.status = status
        task.lastModifiedDate = Instant.now()
        return taskRepository.save(task)
    }

    /**
     * Convert task entity to DTO
     */
    fun toDTO(task: Task): TaskDTO {
        return TaskDTO(
            id = task.id,
            title = task.title,
            description = task.description,
            status = task.status,
            dueDate = task.dueDate,
            assigneeId = task.assignee?.id,
            createdDate = task.createdDate,
            lastModifiedDate = task.lastModifiedDate
        )
    }
}