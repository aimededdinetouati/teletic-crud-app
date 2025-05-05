package com.teletic.test_crud.domain

import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Test sample generators for Token entity
 */
object TokenTestSamples {
    private val random = Random()
    private val countId = AtomicLong((random.nextInt(10) + 1).toLong())

    fun createSampleToken1(user: User): Token {
        return Token(
            id = 1,
            token = "token1",
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(3600),
            validatedAt = Instant.now(),
            user = user
        )
    }

    fun createSampleToken2(user: User): Token {
        return Token(
            id = 2,
            token = "token2",
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(3600),
            validatedAt = Instant.now(),
            user = user
        )
    }

    fun createRandomToken(user: User): Token {
        return Token(
            id = countId.incrementAndGet(),
            token = "token-${UUID.randomUUID()}",
            createdAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(3600),
            validatedAt = Instant.now(),
            user = user
        )
    }
}