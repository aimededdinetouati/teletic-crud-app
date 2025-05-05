package com.teletic.test_crud.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

@Component
class JwtService (
    @Value("\${application.security.jwt.expiration}") private val jwtExpiration: Long,
    @Value("\${application.security.jwt.secret-key}") private val secretKey: String
) {

    fun generateToken(userDetails: UserDetails) : String {
        val extraClaims = HashMap<String, Any>()
        val authorities = userDetails
            .authorities
            .map(GrantedAuthority::getAuthority)
            .toList()

        val currentTimeMillis = Instant.now().toEpochMilli()

        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.username)
            .setIssuedAt(Date(currentTimeMillis))
            .setExpiration(Date(currentTimeMillis + jwtExpiration))
            .claim("authorites", authorities)
            .signWith(getSignInKey())
            .compact()
    }

    fun getSignInKey(): Key {
        val keyBytes: ByteArray = Decoders.BASE64.decode(secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun isTokenValid(token: String, userDetails: UserDetails) : Boolean {
        val username : String = extractUsername(token)
        return (username == userDetails.username) && !isTokenExpired(token)

    }

    fun isTokenExpired(token: String) : Boolean {
        return extractExpiration(token).before(Date())
    }

    fun extractExpiration(
        token: String,
        claimResolver: (Claims) -> Date = Claims::getExpiration
    ): Date {
        val claims: Claims = extractClaims(token)
        return claimResolver(claims)
    }

    fun extractUsername(
        token: String,
        claimResolver: (Claims) -> String = Claims::getSubject
    ): String {
        val claims: Claims = extractClaims(token)
        return claimResolver(claims)
    }

    fun extractClaims(token: String): Claims {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJwt(token)
            .body
    }
}