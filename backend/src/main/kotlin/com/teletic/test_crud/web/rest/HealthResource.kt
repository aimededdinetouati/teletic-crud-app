package com.teletic.test_crud.web.rest

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

@RestController
@RequestMapping("/api/health")
class HealthResource(private val dataSource: DataSource) {

    @GetMapping
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        val status = mutableMapOf("application" to "UP")
        try {
            dataSource.connection.use {
                status["database"] = "UP"
            }
        } catch (e: Exception) {
            status["database"] = "DOWN"
            status["error"] = e.message ?: "Unknown error"
        }
        return ResponseEntity.ok(status)
    }

}