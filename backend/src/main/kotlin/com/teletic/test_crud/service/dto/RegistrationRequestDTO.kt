package com.teletic.test_crud.service.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class RegistrationRequestDTO(
    @NotEmpty @NotBlank val fullName: String,
    @NotEmpty @NotBlank val email: String,
    @NotEmpty @NotBlank @Min(3) val password: String
)
