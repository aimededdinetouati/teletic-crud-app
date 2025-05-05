package com.teletic.test_crud.service.dto

import jakarta.validation.constraints.*

class AuthenticationRequestDTO (
    @field:NotEmpty @field:NotBlank @field:Email
    val email: String,

    @field:NotEmpty @field:NotBlank
    val password: String
)