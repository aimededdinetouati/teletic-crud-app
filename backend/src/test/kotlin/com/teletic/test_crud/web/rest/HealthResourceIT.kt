package com.teletic.test_crud.web.rest

import com.teletic.test_crud.TestCrudApplication
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
@SpringBootTest(classes = [TestCrudApplication::class])
@ActiveProfiles("test")
class HealthResourceIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun testHealthCheck() {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.application").value("UP"))
            .andExpect(jsonPath("$.database").value("UP"))
    }
}