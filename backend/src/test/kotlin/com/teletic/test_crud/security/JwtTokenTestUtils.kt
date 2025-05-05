package com.teletic.test_crud.security.jwt

import com.teletic.test_crud.security.JwtService
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

/**
 * Utility methods for JWT token testing
 */
object JwtTokenTestUtils {
    private const val SECRET_KEY = "e63f7f4a2d5a4a4c273ede1c2ffee2a9349ed6eac7e4ae370da23e39dcd83be2"

    /**
     * Creates a valid token for testing
     * @param userDetails the user details
     * @return the JWT token
     */
    fun createValidToken(userDetails: UserDetails): String {
        val jwtService = JwtService(3600L, SECRET_KEY)
        return jwtService.generateToken(userDetails, HashMap())
    }

    /**
     * Creates an expired token for testing
     * @param userDetails the user details
     * @return the expired JWT token
     */
    fun createExpiredToken(userDetails: UserDetails): String {
        val jwtService = JwtService(-3600L, SECRET_KEY)  // Negative expiration to create expired token
        return jwtService.generateToken(userDetails, HashMap())
    }

    /**
     * Creates a token with invalid signature for testing
     * @param userDetails the user details
     * @return the JWT token with invalid signature
     */
    fun createTokenWithInvalidSignature(userDetails: UserDetails): String {
        val differentKey = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbb"
        val jwtService = JwtService(3600L, differentKey)
        return jwtService.generateToken(userDetails, HashMap())
    }
}