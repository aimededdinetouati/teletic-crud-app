package com.teletic.test_crud.config

import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.RoleRepository
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.security.AuthoritiesConstants
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Database initializer component that runs on application startup.
 * It ensures that required roles and an initial admin user exist in the database.
 */
@Component
class DatabaseInitializer(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    @Transactional
    override fun run(vararg args: String?) {
        log.info("Initializing database with required roles and admin user")

        // Initialize roles if they don't exist
        initializeRoles()

        // Initialize admin user if no admin exists
        initializeAdminUser()

        log.info("Database initialization completed")
    }

    private fun initializeRoles() {
        // Check if ROLE_ADMIN exists, create if it doesn't
        if (roleRepository.findByName(AuthoritiesConstants.ADMIN) == null) {
            log.info("Creating role: {}", AuthoritiesConstants.ADMIN)
            roleRepository.save(Role(name = AuthoritiesConstants.ADMIN))
        }

        // Check if ROLE_USER exists, create if it doesn't
        if (roleRepository.findByName(AuthoritiesConstants.USER) == null) {
            log.info("Creating role: {}", AuthoritiesConstants.USER)
            roleRepository.save(Role(name = AuthoritiesConstants.USER))
        }
    }

    private fun initializeAdminUser() {
        // Fetch ADMIN role
        val adminRole = roleRepository.findByName(AuthoritiesConstants.ADMIN)
            ?: throw IllegalStateException("Admin role not found, database initialization failed")

        // Check if any admin user exists
        val existingAdmins = userRepository.findAll()
            .filter { user -> user.roles.any { it.name == AuthoritiesConstants.ADMIN } }

        if (existingAdmins.isEmpty()) {
            // Create default admin user
            log.info("No admin user found. Creating default admin user")

            val adminUser = User(
                fullName = "Administrator",
                email = "admin@teletic.com",
                password = passwordEncoder.encode("poi321"),
                enabled = true,
                locked = false,
                roles = mutableSetOf(adminRole)
            )

            userRepository.save(adminUser)
            log.info("Default admin user created with email: {}", adminUser.email)
            log.warn("Please change the default admin password after first login!")
        } else {
            log.info("Admin users already exist, skipping admin user creation")
        }
    }
}