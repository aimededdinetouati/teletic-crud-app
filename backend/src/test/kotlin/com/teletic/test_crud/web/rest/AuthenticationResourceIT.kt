package com.teletic.test_crud.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.teletic.test_crud.IntegrationTest
import com.teletic.test_crud.TestCrudApplication
import com.teletic.test_crud.config.EmbeddedSQL
import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.RoleRepository
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.service.dto.AuthenticationRequestDTO
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@AutoConfigureMockMvc
@SpringBootTest(classes = [TestCrudApplication::class])
@EmbeddedSQL
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthenticationResourceIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var userRole: Role

    @BeforeEach
    fun setup() {
        // Create ROLE_USER if it doesn't exist
        userRole = roleRepository.findByName("ROLE_USER")
            ?: roleRepository.save(Role(name = "ROLE_USER"))
    }

    @Test
    @Transactional
    fun testRegister() {
        val registrationCount = userRepository.count()

        val registrationDTO = RegistrationRequestDTO(
            fullName = "Registration Test",
            email = "registration-test@example.com",
            password = "password123"
        )

        // Register a new user
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(registrationDTO))
        )
            .andExpect(status().isAccepted())

        // Check that user was created
        assertThat(userRepository.count()).isEqualTo(registrationCount + 1)

        val user = userRepository.findByEmail(registrationDTO.email)
        assertThat(user).isNotNull
        assertThat(user!!.fullName).isEqualTo(registrationDTO.fullName)
        assertThat(passwordEncoder.matches(registrationDTO.password, user.password)).isTrue()
    }

    @Test
    @Transactional
    fun testRegisterInvalidInput() {
        // Test with invalid email
        val invalidEmailDTO = RegistrationRequestDTO(
            fullName = "Invalid Email",
            email = "not-an-email",
            password = "password123"
        )

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(invalidEmailDTO))
        )
            .andExpect(status().isBadRequest())

        // Test with empty fields
        val emptyFieldsDTO = RegistrationRequestDTO(
            fullName = "",
            email = "",
            password = ""
        )

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(emptyFieldsDTO))
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @Transactional
    fun testLogin() {
        // Create test user
        val email = "login-test@example.com"
        val password = "password123"

        val user = User(
            fullName = "Login Test",
            email = email,
            password = passwordEncoder.encode(password),
            roles = mutableSetOf(userRole)
        )

        userRepository.save(user)

        // Login
        val authRequest = AuthenticationRequestDTO(
            email = email,
            password = password
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(authRequest))
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").isString)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.userId").value(user.id))
    }

    @Test
    @Transactional
    fun testLoginWithInvalidCredentials() {
        // Create test user
        val email = "invalid-login-test@example.com"
        val password = "correct-password"

        val user = User(
            fullName = "Invalid Login Test",
            email = email,
            password = passwordEncoder.encode(password),
            roles = mutableSetOf(userRole)
        )

        userRepository.save(user)

        // Try to login with wrong password
        val authRequest = AuthenticationRequestDTO(
            email = email,
            password = "wrong-password"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(authRequest))
        )
            .andExpect(status().isUnauthorized())
    }

    @Test
    @Transactional
    fun testLoginWithNonExistingUser() {
        // Try to login with non-existing email
        val authRequest = AuthenticationRequestDTO(
            email = "non-existing@example.com",
            password = "any-password"
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(authRequest))
        )
            .andExpect(status().isUnauthorized())
    }
}
