package com.teletic.test_crud.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.teletic.test_crud.IntegrationTest
import com.teletic.test_crud.TestCrudApplication
import com.teletic.test_crud.config.EmbeddedSQL
import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.RoleRepository
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.security.AuthoritiesConstants
import com.teletic.test_crud.service.dto.RegistrationRequestDTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext

@AutoConfigureMockMvc
@WithMockUser(authorities = [AuthoritiesConstants.ADMIN])
@SpringBootTest(classes = [TestCrudApplication::class])
@EmbeddedSQL
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserResourceIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var adminRole: Role
    private lateinit var userRole: Role

    @BeforeEach
    fun setup() {
        roleRepository.deleteAll()
        userRepository.deleteAll()

        // Create roles if they don't exist
        adminRole = roleRepository.save(Role(name = AuthoritiesConstants.ADMIN))

        userRole = roleRepository.save(Role(name = AuthoritiesConstants.USER))
    }

    @Test
    @Transactional
    fun testGetAllUsers() {
        // Initialize the database with two users
        val user1 = User(
            fullName = "Test Admin",
            email = "admin@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(adminRole)
        )
        userRepository.save(user1)

        val user2 = User(
            fullName = "Test User",
            email = "user@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(userRole)
        )
        userRepository.save(user2)

        // Get all users
        mockMvc.perform(get("/users?sort=id,asc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].email").value("admin@example.com"))
            .andExpect(jsonPath("$.content[1].email").value("user@example.com"))
    }

    @Test
    @Transactional
    fun testGetUser() {
        // Initialize the database with a user
        val user = User(
            fullName = "Single Test User",
            email = "single@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(userRole)
        )
        val savedUser = userRepository.save(user)

        // Get the user
        mockMvc.perform(get("/users/{id}", savedUser.id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(savedUser.id))
            .andExpect(jsonPath("$.fullName").value("Single Test User"))
            .andExpect(jsonPath("$.email").value("single@example.com"))
            .andExpect(jsonPath("$.roles[0]").value(AuthoritiesConstants.USER))
    }

    @Test
    @Transactional
    fun testSearchUsers() {
        // Initialize the database with specific test users
        val user1 = User(
            fullName = "John Doe",
            email = "john@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(adminRole)
        )
        userRepository.save(user1)

        val user2 = User(
            fullName = "Jane Doe",
            email = "jane@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(userRole)
        )
        userRepository.save(user2)

        val user3 = User(
            fullName = "Alice Smith",
            email = "alice@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(userRole)
        )
        userRepository.save(user3)

        // Test search by name
        mockMvc.perform(get("/users?fullName=Doe"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[?(@.fullName=='John Doe')]").exists())
            .andExpect(jsonPath("$.content[?(@.fullName=='Jane Doe')]").exists())

        // Test search by email
        mockMvc.perform(get("/users?email=alice"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].fullName").value("Alice Smith"))

        // Test search by role
        mockMvc.perform(get("/users?role=" + AuthoritiesConstants.ADMIN))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].roles[0]").value(AuthoritiesConstants.ADMIN))

        // Test multiple search criteria
        mockMvc.perform(get("/users?fullName=Doe&role=" + AuthoritiesConstants.USER))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].fullName").value("Jane Doe"))
    }

    @Test
    @Transactional
    fun testCreateUser() {
        val countBefore = userRepository.count()

        // Create a new user
        val registrationDTO = RegistrationRequestDTO(
            fullName = "New Test User",
            email = "newuser@example.com",
            password = "password123"
        )

        // Create a regular user
        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(registrationDTO))
            .param("isAdmin", "false"))
            .andExpect(status().isCreated())

        // Verify the user was created correctly
        assertThat(userRepository.count()).isEqualTo(countBefore + 1)
        val createdUser = userRepository.findByEmail("newuser@example.com")
        assertThat(createdUser).isNotNull
        assertThat(createdUser!!.fullName).isEqualTo("New Test User")
        assertThat(createdUser.roles.map { it.name }).containsExactly(AuthoritiesConstants.USER)

        // Create an admin user
        val adminDTO = RegistrationRequestDTO(
            fullName = "New Admin User",
            email = "newadmin@example.com",
            password = "password123"
        )

        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(adminDTO))
            .param("isAdmin", "true"))
            .andExpect(status().isCreated())

        // Verify the admin was created correctly
        val createdAdmin = userRepository.findByEmail("newadmin@example.com")
        assertThat(createdAdmin).isNotNull
        assertThat(createdAdmin!!.fullName).isEqualTo("New Admin User")
        assertThat(createdAdmin.roles.map { it.name }).containsExactly(AuthoritiesConstants.ADMIN)
    }

    @Test
    @Transactional
    fun testCreateUserWithInvalidData() {
        // Test with invalid email
        val invalidEmailDTO = RegistrationRequestDTO(
            fullName = "Invalid User",
            email = "not-an-email",
            password = "password123"
        )

        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(invalidEmailDTO)))
            .andExpect(status().isBadRequest())

        // Test with empty fields
        val emptyFieldsDTO = RegistrationRequestDTO(
            fullName = "",
            email = "",
            password = ""
        )

        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(emptyFieldsDTO)))
            .andExpect(status().isBadRequest())
    }

    @Test
    @Transactional
    fun testUpdateUser() {
        // Create a test user first
        val user = User(
            fullName = "Original Name",
            email = "original@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(userRole)
        )
        val savedUser = userRepository.save(user)

        // Update the user
        val updateDTO = RegistrationRequestDTO(
            fullName = "Updated Name",
            email = "updated@example.com",
            password = "newpassword"
        )

        mockMvc.perform(put("/users/{userId}", savedUser.id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(updateDTO))
            .param("isAdmin", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Updated Name"))
            .andExpect(jsonPath("$.email").value("updated@example.com"))
            .andExpect(jsonPath("$.roles[0]").value(AuthoritiesConstants.ADMIN))

        // Verify the database was updated correctly
        val updatedUser = userRepository.findById(savedUser.id!!).get()
        assertThat(updatedUser.fullName).isEqualTo("Updated Name")
        assertThat(updatedUser.email).isEqualTo("updated@example.com")
        assertThat(updatedUser.roles.map { it.name }).containsExactly(AuthoritiesConstants.ADMIN)
    }

    @Test
    @Transactional
    fun testUpdateNonExistingUser() {
        val updateDTO = RegistrationRequestDTO(
            fullName = "Non Existing",
            email = "nonexisting@example.com",
            password = "password"
        )

        mockMvc.perform(put("/users/999999")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(updateDTO)))
            .andExpect(status().isNotFound)
    }

    @Test
    @Transactional
    fun testDeleteUser() {
        // Create a test user first
        val user = User(
            fullName = "User To Delete",
            email = "delete@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(userRole)
        )
        val savedUser = userRepository.save(user)

        // Delete the user
        mockMvc.perform(delete("/users/{userId}", savedUser.id))
            .andExpect(status().isNoContent())

        // Verify the user was deleted
        assertThat(userRepository.findById(savedUser.id!!)).isEmpty
    }

    @Test
    @Transactional
    fun testDeleteLastAdminUser() {
        // Clear all existing users
        userRepository.deleteAll()

        // Create a single admin user
        val admin = User(
            fullName = "Last Admin",
            email = "lastadmin@example.com",
            password = passwordEncoder.encode("password"),
            roles = mutableSetOf(adminRole)
        )
        val savedAdmin = userRepository.save(admin)

        // Try to delete the last admin - should fail
        mockMvc.perform(delete("/users/{userId}", savedAdmin.id))
            .andExpect(status().isBadRequest)

        // Verify admin still exists
        assertThat(userRepository.findById(savedAdmin.id!!)).isPresent
    }
}