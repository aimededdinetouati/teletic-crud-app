package com.teletic.test_crud.repository

import com.teletic.test_crud.domain.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName (name: String): Role?
}