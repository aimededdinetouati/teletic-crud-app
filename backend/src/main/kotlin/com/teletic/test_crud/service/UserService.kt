package com.teletic.test_crud.service

import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.security.AuthoritiesConstants
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import com.teletic.test_crud.service.dto.UserDTO
import com.teletic.test_crud.web.rest.errors.BadRequestAlertException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService (
    private val userRepository: UserRepository,
    private val roleService: RoleService
) {

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun findById(id: Long): User {
        return userRepository.findById(id).orElseThrow {
            BadRequestAlertException("User not found", "userManagement", "userNotFound")
        }
    }

    fun save(user: User): User {
        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    fun search(fullName: String?, email: String?, role: String?, pageable: Pageable): Page<User> {
        val fnFilter = fullName
            ?.takeIf { it.isNotBlank() }
            ?.lowercase()
            ?.let { "%$it%" }

        val emFilter = email
            ?.takeIf { it.isNotBlank() }
            ?.lowercase()
            ?.let { "%$it%" }
        return userRepository.search(fnFilter, emFilter, role, pageable)
    }

    fun update(userId: Long, registrationRequestDTO: RegistrationRequestDTO, isAdmin: Boolean): User {
        val user = findById(userId)
        val role: Role = roleService.findByFlag(isAdmin)
        populateUserFields(user, registrationRequestDTO, role)
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
            throw BadRequestAlertException("Cannot delete last admin user", "error", "Last admin user cannot be deleted")
        }
        userRepository.deleteById(id)
    }

    /**
     * Converts User entity to UserDTO, excluding sensitive information
     */
    fun toDTO(user: User): UserDTO {
        return UserDTO(
            id = user.id!!,
            fullName = user.fullName,
            email = user.email,
            roles = user.roles.map { it.name },
            enabled = user.enabled,
            locked = user.locked,
            createdDate = user.createdDate,
            lastModifiedDate = user.lastModifiedDate
        )
    }
}