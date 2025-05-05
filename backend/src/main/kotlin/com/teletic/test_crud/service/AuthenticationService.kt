package com.teletic.test_crud.service

import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.security.JwtService
import com.teletic.test_crud.service.dto.AuthenticationRequestDTO
import com.teletic.test_crud.service.dto.AuthenticationResponseDTO
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService
) {
    private val log = LoggerFactory.getLogger(AuthenticationService::class.java)

    fun register(requestDTO: RegistrationRequestDTO, isAdmin: Boolean) {
        val role: Role = roleService.findByFlag(isAdmin)
        register(requestDTO, role)
    }

    fun register(requestDTO: RegistrationRequestDTO, role: Role) {
        val user = User(
            fullName = requestDTO.fullName,
            email = requestDTO.email,
            password = passwordEncoder.encode(requestDTO.password),
            roles = mutableSetOf(role)
        )
        userRepository.save(user)
        log.debug("The user with email {} has been saved successfully", user.email)
    }

    fun authenticate(requestDTO: AuthenticationRequestDTO): Any? {
        val auth = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                requestDTO.email,
                requestDTO.password
            )
        )

        val claims = HashMap<String, Any>()
        val user = auth.principal as User
        claims["fullName"] = user.fullName
        claims["userId"] = user.id ?: throw IllegalStateException("User ID cannot be null")
        val jwtToken = jwtService.generateToken(user, claims)
        return AuthenticationResponseDTO(token = jwtToken, userId = user.id!!)
    }


}