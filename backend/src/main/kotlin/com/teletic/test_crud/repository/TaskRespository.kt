package com.teletic.test_crud.repository

import com.teletic.test_crud.domain.Task
import com.teletic.test_crud.domain.TaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TaskRepository : JpaRepository<Task, Long> {

    /**
     * Find all tasks assigned to a specific user
     */
    fun findByAssigneeId(userId: Long, pageable: Pageable): Page<Task>

    /**
     * Find all tasks assigned to a specific user with a specific status
     */
    fun findByAssigneeIdAndStatus(userId: Long, status: TaskStatus, pageable: Pageable): Page<Task>

    /**
     * Search tasks based on criteria
     */
    @Query("""
        SELECT t FROM Task t
        LEFT JOIN t.assignee u
        WHERE (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:status IS NULL OR t.status = :status)
        AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
    """)
    fun search(
        @Param("title") title: String?,
        @Param("status") status: TaskStatus?,
        @Param("assigneeId") assigneeId: Long?,
        pageable: Pageable
    ): Page<Task>
}