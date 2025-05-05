package com.teletic.test_crud.web.rest

import com.teletic.test_crud.service.AuthenticationService
import com.teletic.test_crud.service.UserService
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Tag(name = "User")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
class UserResource (
    private val authenticationService: AuthenticationService,
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(UserResource::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createUser(@RequestBody @Valid registrationRequestDTO: RegistrationRequestDTO,
                   @RequestParam(defaultValue = "false") isAdmin: Boolean): ResponseEntity<Any> {
        log.info("Registering new user with email: {}", registrationRequestDTO.email)
        authenticationService.register(registrationRequestDTO, isAdmin)
        return ResponseEntity.accepted().build()
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateUser(@PathVariable userId: Long,
                   @RequestBody @Valid registrationRequestDTO: RegistrationRequestDTO,
                   @RequestParam(defaultValue = "false") isAdmin: Boolean): ResponseEntity<Any> {
        log.info("Updating user with id: {}", userId)
        val result = userService.update(userId, registrationRequestDTO, isAdmin)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable userId: Long) {
        log.info("Deleting user with id: {}", userId)
        userService.delete(userId)
    }

}