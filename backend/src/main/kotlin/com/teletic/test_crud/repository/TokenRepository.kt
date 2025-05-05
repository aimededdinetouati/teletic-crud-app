package com.teletic.test_crud.repository

import com.teletic.test_crud.domain.Token
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long> {
    fun findByToken(token: String): Token?
}