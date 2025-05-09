package com.teletic.test_crud.web.rest

import com.teletic.test_crud.service.AuthenticationService
import com.teletic.test_crud.service.dto.AuthenticationRequestDTO
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
class AuthenticationResource (
    private val authenticationService: AuthenticationService
) {
    private val log = LoggerFactory.getLogger(AuthenticationResource::class.java)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun register (
        @RequestBody @Valid registrationRequestDTO: RegistrationRequestDTO
    ): ResponseEntity<Any> {
        log.info("Registering new user with email: {}", registrationRequestDTO.email)
        authenticationService.register(registrationRequestDTO, true)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/login")
    fun login (
        @RequestBody @Valid requestDTO: AuthenticationRequestDTO
    ): ResponseEntity<Any> {
        log.info("Authentication user with email: {}", requestDTO.email)
        var result = authenticationService.authenticate(requestDTO)!!
        return ResponseEntity.ok(result)
    }

}