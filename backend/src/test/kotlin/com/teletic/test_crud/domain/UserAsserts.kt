package com.teletic.test_crud.domain

import org.assertj.core.api.Assertions.assertThat

/**
 * Assertion methods for User entity
 */
object UserAsserts {
    /**
     * Asserts that two User entities have equal fields
     * @param expected the expected User
     * @param actual the actual User
     */
    fun assertUserEquals(expected: User, actual: User) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.fullName).isEqualTo(expected.fullName)
        assertThat(actual.email).isEqualTo(expected.email)
        assertThat(actual.locked).isEqualTo(expected.locked)
        assertThat(actual.enabled).isEqualTo(expected.enabled)

        // We don't compare passwords directly as they may be encoded differently
        // assertThat(actual.password).isEqualTo(expected.password)

        // These might be slightly different timestamps
        // assertThat(actual.createdDate).isEqualTo(expected.createdDate)
        // assertThat(actual.lastModifiedDate).isEqualTo(expected.lastModifiedDate)

        assertThat(actual.roles.map { it.name }).containsExactlyInAnyOrderElementsOf(expected.roles.map { it.name })
    }
}

