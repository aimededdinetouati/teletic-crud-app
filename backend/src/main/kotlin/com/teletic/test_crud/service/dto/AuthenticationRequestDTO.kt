package com.teletic.test_crud.service.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

class AuthenticationRequestDTO (
    @NotEmpty @NotBlank val email: String,
    @NotEmpty @NotBlank @Min(3) val password: String
)