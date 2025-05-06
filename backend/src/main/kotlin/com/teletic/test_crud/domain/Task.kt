package com.teletic.test_crud.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener::class)
class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskStatus = TaskStatus.TO_DO,

    @Column
    var dueDate: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    var assignee: User? = null,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDate: Instant? = null,

    @LastModifiedDate
    @Column
    var lastModifiedDate: Instant? = null
)

enum class TaskStatus {
    TO_DO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}