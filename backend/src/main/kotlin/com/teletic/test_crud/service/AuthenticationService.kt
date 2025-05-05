package com.teletic.test_crud.service

import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.RoleRepository
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LoggerFactory.getLogger(AuthenticationService::class.java)

    fun register(requestDTO: RegistrationRequestDTO) {
        val userRole: Role = roleRepository.findByName("ROLE_USER")
            ?: throw IllegalStateException("ROLE_USER not found in the database")

        val user = User(
            fullName = requestDTO.fullName,
            email = requestDTO.email,
            password = passwordEncoder.encode(requestDTO.password),
            roles = mutableSetOf(userRole)
        )
        userRepository.save(user)
        log.debug("The user with email {} has been saved successfully", user.email)
    }

}