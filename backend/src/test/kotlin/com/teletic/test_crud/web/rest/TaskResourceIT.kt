package com.teletic.test_crud.web.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.teletic.test_crud.TestCrudApplication
import com.teletic.test_crud.domain.Role
import com.teletic.test_crud.domain.Task
import com.teletic.test_crud.domain.TaskStatus
import com.teletic.test_crud.domain.User
import com.teletic.test_crud.repository.RoleRepository
import com.teletic.test_crud.repository.TaskRepository
import com.teletic.test_crud.repository.UserRepository
import com.teletic.test_crud.security.AuthoritiesConstants
import com.teletic.test_crud.service.dto.TaskAssignmentDTO
import com.teletic.test_crud.service.dto.TaskDTO
import com.teletic.test_crud.service.dto.TaskStatusUpdateDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.function.Consumer

@AutoConfigureMockMvc
@WithMockUser(username = "admin@example.com", authorities = [AuthoritiesConstants.ADMIN])
@SpringBootTest(classes = [TestCrudApplication::class])
@ActiveProfiles("test")
class TaskResourceIT {

    @Autowired
    private lateinit var taskRepository: TaskRepository

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
    private lateinit var adminUser: User
    private lateinit var regularUser: User
    private lateinit var task: Task

    @BeforeEach
    fun setup() {
        roleRepository.deleteAll()
        taskRepository.deleteAll()
        userRepository.deleteAll()

        // Create roles if they don't exist
        val adminRoleFromDb = roleRepository.findByName(AuthoritiesConstants.ADMIN)
        adminRole = adminRoleFromDb ?: roleRepository.save(Role(name = AuthoritiesConstants.ADMIN))

        val userRoleFromDb = roleRepository.findByName(AuthoritiesConstants.USER)
        userRole = userRoleFromDb ?: roleRepository.save(Role(name = AuthoritiesConstants.USER))


        // Create test users if they don't exist
        if (userRepository.findByEmail("admin@example.com") == null) {
            adminUser = User(
                fullName = "Admin User",
                email = "admin@example.com",
                password = passwordEncoder.encode("password"),
                roles = mutableSetOf(adminRole)
            )
            adminUser = userRepository.save(adminUser)
        } else {
            adminUser = userRepository.findByEmail("admin@example.com")!!
        }

        if (userRepository.findByEmail("user@example.com") == null) {
            regularUser = User(
                fullName = "Regular User",
                email = "user@example.com",
                password = passwordEncoder.encode("password"),
                roles = mutableSetOf(userRole)
            )
            regularUser = userRepository.save(regularUser)
        } else {
            regularUser = userRepository.findByEmail("user@example.com")!!
        }
    }

    @Test
    @Transactional
    fun createTask() {
        val databaseSizeBeforeCreate = taskRepository.findAll().size

        // Create the Task
        val taskDTO = TaskDTO(
            title = "Test Task",
            description = "This is a test task description",
            status = TaskStatus.TO_DO,
            dueDate = Instant.now().plus(7, ChronoUnit.DAYS)
        )

        mockMvc.perform(
            post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(taskDTO))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.title").value("Test Task"))
            .andExpect(jsonPath("$.description").value("This is a test task description"))
            .andExpect(jsonPath("$.status").value("TO_DO"))

        // Validate the Task in the database
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeCreate + 1)
        val testTask = taskList.last()
        assertThat(testTask.title).isEqualTo("Test Task")
        assertThat(testTask.description).isEqualTo("This is a test task description")
        assertThat(testTask.status).isEqualTo(TaskStatus.TO_DO)
    }

    @Test
    @Transactional
    fun createTaskWithExistingId() {
        // Create a task with a specific ID
        val task = Task(
            id = 1L,
            title = "Existing Task",
            status = TaskStatus.TO_DO
        )
        taskRepository.saveAndFlush(task)

        val databaseSizeBeforeCreate = taskRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        val taskDTO = TaskDTO(
            id = task.id,
            title = "Different Title",
            status = TaskStatus.IN_PROGRESS
        )

        mockMvc.perform(
            post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(taskDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the Task in the database
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllTasks() {
        // Initialize the database
        val task1 = Task(
            title = "Task 1",
            description = "Description 1",
            status = TaskStatus.TO_DO
        )
        taskRepository.save(task1)

        val task2 = Task(
            title = "Task 2",
            description = "Description 2",
            status = TaskStatus.IN_PROGRESS,
            assignee = regularUser
        )
        taskRepository.save(task2)

        // Get all the tasks
        mockMvc.perform(get("/tasks?sort=id,asc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Task 1"))
            .andExpect(jsonPath("$.content[1].title").value("Task 2"))
            .andExpect(jsonPath("$.content[1].assigneeId").value(regularUser.id))
    }

    @Test
    @Transactional
    fun getTask() {
        // Initialize the database
        val task = Task(
            title = "Task to Get",
            description = "Description to Get",
            status = TaskStatus.TO_DO,
            assignee = regularUser
        )
        taskRepository.saveAndFlush(task)

        // Get the task
        mockMvc.perform(get("/tasks/{id}", task.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(task.id))
            .andExpect(jsonPath("$.title").value("Task to Get"))
            .andExpect(jsonPath("$.description").value("Description to Get"))
            .andExpect(jsonPath("$.status").value("TO_DO"))
            .andExpect(jsonPath("$.assigneeId").value(regularUser.id))
    }

    @Test
    @Transactional
    fun getNonExistingTask() {
        // Get the task
        mockMvc.perform(get("/tasks/{id}", Long.MAX_VALUE))
            .andExpect(status().isBadRequest)
    }

    @Test
    @Transactional
    @WithMockUser(username = "user@example.com", authorities = [AuthoritiesConstants.USER])
    fun getTaskAsRegularUser() {
        // Initialize the database
        val task1 = Task(
            title = "Assigned Task",
            description = "Description",
            status = TaskStatus.TO_DO,
            assignee = regularUser
        )
        taskRepository.saveAndFlush(task1)

        val task2 = Task(
            title = "Unassigned Task",
            description = "Description",
            status = TaskStatus.TO_DO
        )
        taskRepository.saveAndFlush(task2)

        // Get assigned task (should succeed)
        mockMvc.perform(get("/tasks/{id}", task1.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Assigned Task"))

        // Get unassigned task (should fail with forbidden)
        mockMvc.perform(get("/tasks/{id}", task2.id))
            .andExpect(status().isForbidden)
    }

    @Test
    @Transactional
    fun updateTask() {
        // Initialize the database
        val task = Task(
            title = "Original Task",
            description = "Original Description",
            status = TaskStatus.TO_DO
        )
        taskRepository.saveAndFlush(task)

        val databaseSizeBeforeUpdate = taskRepository.findAll().size

        // Update the task
        val updatedTask = taskRepository.findById(task.id!!).get()
        val updatedTaskDTO = TaskDTO(
            id = updatedTask.id,
            title = "Updated Task",
            description = "Updated Description",
            status = TaskStatus.IN_PROGRESS,
            dueDate = Instant.now().plus(10, ChronoUnit.DAYS)
        )

        mockMvc.perform(
            put("/tasks/{id}", updatedTask.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updatedTaskDTO))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(updatedTask.id))
            .andExpect(jsonPath("$.title").value("Updated Task"))
            .andExpect(jsonPath("$.description").value("Updated Description"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))

        // Validate the Task in the database
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate)
        val testTask = taskList.first { it.id == updatedTask.id }
        assertThat(testTask.title).isEqualTo("Updated Task")
        assertThat(testTask.description).isEqualTo("Updated Description")
        assertThat(testTask.status).isEqualTo(TaskStatus.IN_PROGRESS)
    }

    @Test
    @Transactional
    fun updateNonExistingTask() {
        val databaseSizeBeforeUpdate = taskRepository.findAll().size

        // If the task doesn't exist, it should return bad request
        val taskDTO = TaskDTO(
            id = Long.MAX_VALUE,
            title = "Non-existing Task",
            status = TaskStatus.TO_DO
        )

        mockMvc.perform(
            put("/tasks/{id}", Long.MAX_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(taskDTO))
        )
            .andExpect(status().isBadRequest)

        // Validate the database contains the same number of tasks
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteTask() {
        // Initialize the database
        val task = Task(
            title = "Task to Delete",
            description = "Description to Delete",
            status = TaskStatus.TO_DO
        )
        taskRepository.saveAndFlush(task)

        val databaseSizeBeforeDelete = taskRepository.findAll().size

        // Delete the task
        mockMvc.perform(
            delete("/tasks/{id}", task.id)
        )
            .andExpect(status().isNoContent)

        // Validate the database contains one less item
        val taskList = taskRepository.findAll()
        assertThat(taskList).hasSize(databaseSizeBeforeDelete - 1)
        assertThat(taskRepository.findById(task.id!!)).isEmpty
    }

    @Test
    @Transactional
    fun assignTask() {
        // Initialize the database
        val task = Task(
            title = "Task to Assign",
            description = "Description",
            status = TaskStatus.TO_DO
        )
        taskRepository.saveAndFlush(task)

        // Assign the task
        val assignmentDTO = TaskAssignmentDTO(userId = regularUser.id!!)

        mockMvc.perform(
            patch("/tasks/{id}/assign", task.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(assignmentDTO))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(task.id))
            .andExpect(jsonPath("$.assigneeId").value(regularUser.id))

        // Validate the Task in the database
        val updatedTask = taskRepository.findById(task.id!!).get()
        assertThat(updatedTask.assignee?.id).isEqualTo(regularUser.id)
    }

    @Test
    @Transactional
    @WithMockUser(username = "user@example.com", authorities = [AuthoritiesConstants.USER])
    fun getUserTasks() {
        // Initialize the database with tasks for the user
        val task1 = Task(
            title = "User Task 1",
            description = "Description 1",
            status = TaskStatus.TO_DO,
            assignee = regularUser
        )
        taskRepository.save(task1)

        val task2 = Task(
            title = "User Task 2",
            description = "Description 2",
            status = TaskStatus.IN_PROGRESS,
            assignee = regularUser
        )
        taskRepository.save(task2)

        val task3 = Task(
            title = "Another User Task",
            description = "Description 3",
            status = TaskStatus.COMPLETED,
            assignee = adminUser
        )
        taskRepository.save(task3)

        // Get all tasks assigned to the regular user
        mockMvc.perform(get("/tasks/my?sort=id,asc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("User Task 1"))
            .andExpect(jsonPath("$.content[1].title").value("User Task 2"))

        // Get tasks with a specific status
        mockMvc.perform(get("/tasks/my?status=IN_PROGRESS"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].title").value("User Task 2"))
            .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"))
    }

    @Test
    @Transactional
    fun updateTaskStatus() {
        // Initialize the database
        val task = Task(
            title = "Status Update Task",
            description = "Description",
            status = TaskStatus.TO_DO,
            assignee = regularUser
        )
        taskRepository.saveAndFlush(task)

        // Update the task status
        val statusUpdateDTO = TaskStatusUpdateDTO(status = TaskStatus.COMPLETED)

        mockMvc.perform(
            patch("/tasks/{id}/status", task.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(statusUpdateDTO))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(task.id))
            .andExpect(jsonPath("$.status").value("COMPLETED"))

        // Validate the Task in the database
        val updatedTask = taskRepository.findById(task.id!!).get()
        assertThat(updatedTask.status).isEqualTo(TaskStatus.COMPLETED)
    }

    @Test
    @Transactional
    @WithMockUser(username = "user@example.com", authorities = [AuthoritiesConstants.USER])
    fun updateTaskStatusAsAssignedUser() {
        // Initialize the database with a task assigned to the regular user
        val task = Task(
            title = "User Status Update Task",
            description = "Description",
            status = TaskStatus.TO_DO,
            assignee = regularUser
        )
        taskRepository.saveAndFlush(task)

        // Update the task status
        val statusUpdateDTO = TaskStatusUpdateDTO(status = TaskStatus.IN_PROGRESS)

        mockMvc.perform(
            patch("/tasks/{id}/status", task.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(statusUpdateDTO))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(task.id))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))

        // Validate the Task in the database
        val updatedTask = taskRepository.findById(task.id!!).get()
        assertThat(updatedTask.status).isEqualTo(TaskStatus.IN_PROGRESS)
    }

    @Test
    @Transactional
    @WithMockUser(username = "user@example.com", authorities = [AuthoritiesConstants.USER])
    fun updateTaskStatusAsNonAssignedUser() {
        // Initialize the database with a task NOT assigned to the regular user
        val task = Task(
            title = "Admin Task",
            description = "Description",
            status = TaskStatus.TO_DO,
            assignee = adminUser
        )
        taskRepository.saveAndFlush(task)

        // Try to update the task status
        val statusUpdateDTO = TaskStatusUpdateDTO(status = TaskStatus.IN_PROGRESS)

        mockMvc.perform(
            patch("/tasks/{id}/status", task.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(statusUpdateDTO))
        )
            .andExpect(status().isForbidden)

        // Validate the Task in the database - status should not have changed
        val unchangedTask = taskRepository.findById(task.id!!).get()
        assertThat(unchangedTask.status).isEqualTo(TaskStatus.TO_DO)
    }

    @Test
    @Transactional
    fun searchTasksByTitle() {
        // Initialize the database
        val task1 = Task(
            title = "Project Planning",
            description = "Description 1",
            status = TaskStatus.TO_DO
        )
        taskRepository.save(task1)

        val task2 = Task(
            title = "Project Implementation",
            description = "Description 2",
            status = TaskStatus.TO_DO
        )
        taskRepository.save(task2)

        val task3 = Task(
            title = "Bug Fixing",
            description = "Description 3",
            status = TaskStatus.TO_DO
        )
        taskRepository.save(task3)

        // Search for tasks with "Project" in the title
        mockMvc.perform(get("/tasks?title=Project&sort=id,asc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Project Planning"))
            .andExpect(jsonPath("$.content[1].title").value("Project Implementation"))
    }

    @Test
    @Transactional
    fun searchTasksByStatus() {
        // Initialize the database
        val task1 = Task(
            title = "Task 1",
            description = "Description 1",
            status = TaskStatus.TO_DO
        )
        taskRepository.save(task1)

        val task2 = Task(
            title = "Task 2",
            description = "Description 2",
            status = TaskStatus.IN_PROGRESS
        )
        taskRepository.save(task2)

        val task3 = Task(
            title = "Task 3",
            description = "Description 3",
            status = TaskStatus.IN_PROGRESS
        )
        taskRepository.save(task3)

        // Search for tasks with IN_PROGRESS status
        mockMvc.perform(get("/tasks?status=IN_PROGRESS&sort=id,asc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Task 2"))
            .andExpect(jsonPath("$.content[1].title").value("Task 3"))
            .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.content[1].status").value("IN_PROGRESS"))
    }

    @Test
    @Transactional
    fun searchTasksByAssignee() {
        // Initialize the database
        val task1 = Task(
            title = "Admin Task",
            description = "Description 1",
            status = TaskStatus.TO_DO,
            assignee = adminUser
        )
        taskRepository.save(task1)

        val task2 = Task(
            title = "User Task 1",
            description = "Description 2",
            status = TaskStatus.TO_DO,
            assignee = regularUser
        )
        taskRepository.save(task2)

        val task3 = Task(
            title = "User Task 2",
            description = "Description 3",
            status = TaskStatus.TO_DO,
            assignee = regularUser
        )
        taskRepository.save(task3)

        // Search for tasks assigned to the regular user
        mockMvc.perform(get("/tasks?assigneeId={userId}&sort=id,asc", regularUser.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("User Task 1"))
            .andExpect(jsonPath("$.content[1].title").value("User Task 2"))
            .andExpect(jsonPath("$.content[0].assigneeId").value(regularUser.id))
            .andExpect(jsonPath("$.content[1].assigneeId").value(regularUser.id))
    }
}