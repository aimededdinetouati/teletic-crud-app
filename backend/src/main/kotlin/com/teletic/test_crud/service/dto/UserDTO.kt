package com.teletic.test_crud.service.dto

import java.time.Instant

data class UserDTO(
    val id: Long,
    val fullName: String,
    val email: String,
    val roles: List<String>,
    val enabled: Boolean,
    val locked: Boolean,
    val createdDate: Instant?,
    val lastModifiedDate: Instant?
)