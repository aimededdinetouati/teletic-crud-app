package com.teletic.test_crud.domain

import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Test sample generators for User entity
 */
object UserTestSamples {
    private val random = Random()
    private val countId = AtomicLong((random.nextInt(10) + 1).toLong())

    fun createSampleUser1(): User {
        return User(
            id = 1,
            fullName = "Test User 1",
            email = "test1@example.com",
            password = "password1",
            locked = false,
            enabled = true,
            createdDate = Instant.now(),
            lastModifiedDate = Instant.now()
        )
    }

    fun createSampleUser2(): User {
        return User(
            id = 2,
            fullName = "Test User 2",
            email = "test2@example.com",
            password = "password2",
            locked = false,
            enabled = true,
            createdDate = Instant.now(),
            lastModifiedDate = Instant.now()
        )
    }

    fun createRandomUser(): User {
        return User(
            id = countId.incrementAndGet(),
            fullName = "User ${UUID.randomUUID()}",
            email = "user-${UUID.randomUUID()}@example.com",
            password = "password${UUID.randomUUID()}",
            locked = false,
            enabled = true,
            createdDate = Instant.now(),
            lastModifiedDate = Instant.now()
        )
    }
}