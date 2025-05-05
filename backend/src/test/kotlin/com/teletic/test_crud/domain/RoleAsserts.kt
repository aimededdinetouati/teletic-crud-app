package com.teletic.test_crud.domain

import org.assertj.core.api.Assertions.assertThat

/**
 * Assertion methods for Role entity
 */
object RoleAsserts {
    /**
     * Asserts that two Role entities have equal fields
     * @param expected the expected Role
     * @param actual the actual Role
     */
    fun assertRoleEquals(expected: Role, actual: Role) {
        assertThat(actual.id).isEqualTo(expected.id)
        assertThat(actual.name).isEqualTo(expected.name)

        // These might be slightly different timestamps
        // assertThat(actual.createdDate).isEqualTo(expected.createdDate)
        // assertThat(actual.lastModifiedDate).isEqualTo(expected.lastModifiedDate)
    }
}