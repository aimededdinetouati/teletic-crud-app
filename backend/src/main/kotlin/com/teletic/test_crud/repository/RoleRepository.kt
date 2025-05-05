package com.teletic.test_crud.repository

import org.springframework.data.jpa.repository.JpaRepository
import javax.management.relation.Role

interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName (name: String): Role?
}