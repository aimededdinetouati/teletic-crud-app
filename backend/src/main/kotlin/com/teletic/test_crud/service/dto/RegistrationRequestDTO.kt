package com.teletic.test_crud.service.dto

import jakarta.validation.constraints.*

data class RegistrationRequestDTO(
    @field:NotEmpty @field:NotBlank
    val fullName: String,

    @field:NotEmpty @field:NotBlank @field:Email
    val email: String,

    @field:NotEmpty @field:NotBlank @field:Size(min = 3)
    val password: String
)
