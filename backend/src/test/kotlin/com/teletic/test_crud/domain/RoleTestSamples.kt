package com.teletic.test_crud.domain

import com.teletic.test_crud.security.AuthoritiesConstants
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Test sample generators for Role entity
 */
object RoleTestSamples {
    private val random = Random()
    private val countId = AtomicLong((random.nextInt(10) + 1).toLong())

    fun createSampleRole1(): Role {
        return Role(
            id = 1,
            name = "ROLE_USER",
            createdDate = Instant.now(),
            lastModifiedDate = Instant.now()
        )
    }

    fun createSampleRole2(): Role {
        return Role(
            id = 2,
            name = AuthoritiesConstants.ADMIN,
            createdDate = Instant.now(),
            lastModifiedDate = Instant.now()
        )
    }

    fun createRandomRole(): Role {
        return Role(
            id = countId.incrementAndGet(),
            name = "ROLE_${UUID.randomUUID()}",
            createdDate = Instant.now(),
            lastModifiedDate = Instant.now()
        )
    }
}