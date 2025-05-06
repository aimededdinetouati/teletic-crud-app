package com.teletic.test_crud.config

import com.teletic.test_crud.repository.UserRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

@TestConfiguration
@Profile("test")
class TestSecurityConfig(private val userRepository: UserRepository) {

    /**
     * Custom UserDetailsService for tests that loads users from the repository
     */
    @Bean
    @Primary
    fun userDetailsService(): UserDetailsService {
        return UserDetailsService { username ->
            userRepository.findByEmail(username)
                ?: throw UsernameNotFoundException("User $username not found in database")
        }
    }
}