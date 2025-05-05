package com.teletic.test_crud.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.Instant

@Entity
class Token (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val token: String,

    val createdAt: Instant,

    val expiresAt: Instant,

    val validatedAt: Instant,

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    val user: User,
)