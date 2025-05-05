package com.teletic.test_crud.web.rest

import com.teletic.test_crud.service.AuthenticationService
import com.teletic.test_crud.service.UserService
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import com.teletic.test_crud.service.dto.UserDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/users")
@Tag(name = "User Management")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
class UserResource(
    private val authenticationService: AuthenticationService,
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(UserResource::class.java)

    @GetMapping
    @Operation(summary = "Get all users with pagination and searching capabilities")
    fun getAllUsers(
        @Parameter(description = "Filter by name (optional)") @RequestParam(required = false) fullName: String?,
        @Parameter(description = "Filter by email (optional)") @RequestParam(required = false) email: String?,
        @Parameter(description = "Filter by role (optional)") @RequestParam(required = false) role: String?,
        @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<UserDTO>> {
        log.debug("REST request to get users with filters - fullName: {}, email: {}, role: {}", fullName, email, role)

        val page = if (fullName != null || email != null || role != null) {
            userService.search(fullName, email, role, pageable)
        } else {
            userService.findAll(pageable)
        }

        return ResponseEntity.ok(page.map { userService.toDTO(it) })
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a specific user by ID")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserDTO> {
        log.debug("REST request to get User with ID: {}", userId)
        val user = userService.findById(userId)
        return ResponseEntity.ok(userService.toDTO(user))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user")
    fun createUser(
        @RequestBody @Valid registrationRequestDTO: RegistrationRequestDTO,
        @Parameter(description = "Set to true to create an admin user")
        @RequestParam(defaultValue = "false") isAdmin: Boolean
    ): ResponseEntity<Void> {
        log.info("REST request to create a new user with email: {}", registrationRequestDTO.email)
        authenticationService.register(registrationRequestDTO, isAdmin)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update an existing user")
    fun updateUser(
        @PathVariable userId: Long,
        @RequestBody @Valid registrationRequestDTO: RegistrationRequestDTO,
        @Parameter(description = "Set to true to make the user an admin")
        @RequestParam(defaultValue = "false") isAdmin: Boolean
    ): ResponseEntity<UserDTO> {
        log.info("REST request to update user with id: {}", userId)
        val result = userService.update(userId, registrationRequestDTO, isAdmin)
        return ResponseEntity.ok(userService.toDTO(result))
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Void> {
        log.info("REST request to delete user with id: {}", userId)
        try {
            userService.delete(userId)
            return ResponseEntity.noContent().build()
        } catch (e: IllegalStateException) {
            log.warn("Cannot delete user with id: {} - {}", userId, e.message)
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}