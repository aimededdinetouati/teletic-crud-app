package com.teletic.test_crud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class TestCrudApplication

fun main(args: Array<String>) {
	runApplication<TestCrudApplication>(*args)
}
