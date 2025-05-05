package com.teletic.test_crud.service

import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.repository.RoleRepository
import com.teletic.test_crud.security.AuthoritiesConstants
import org.springframework.stereotype.Service

@Service
class RoleService (
    private val roleRepository: RoleRepository
) {

    fun findByFlag(isAdmin: Boolean): Role {
        return  if (isAdmin) {
            findByRoleName(AuthoritiesConstants.ADMIN)
        } else {
            findByRoleName(AuthoritiesConstants.USER)
        }
    }

    fun findByRoleName(roleName: String): Role {
        return roleRepository.findByName(roleName)
            ?: throw IllegalStateException("ROLE_ADMIN not found in the database")
    }
}