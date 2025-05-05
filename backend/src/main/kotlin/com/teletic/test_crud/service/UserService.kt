package com.teletic.test_crud.service

import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.security.AuthoritiesConstants
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository,
    private val roleService: RoleService
) {

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow { IllegalStateException("User not found") }
    }


    fun save(user: User): User {
        return userRepository.save(user)
    }

    fun update(userId: Long, RegistrationRequestDTO: RegistrationRequestDTO, isAdmin: Boolean): User {
        val user = findById(userId)
        val role: Role = roleService.findByFlag(isAdmin)
        populateUserFields(user, RegistrationRequestDTO, role)
        return save(user)
    }

    fun populateUserFields(user: User, requestDTO: RegistrationRequestDTO, role: Role) {
        user.fullName = requestDTO.fullName
        user.email = requestDTO.email
        user.roles = mutableSetOf(role)
    }

    fun delete(id: Long) {
        findById(id)
        if (userRepository.isLastUserWithRole(id, AuthoritiesConstants.ADMIN)) {
            throw IllegalStateException("Cannot delete last admin user")
        }
        userRepository.deleteById(id)
    }

}