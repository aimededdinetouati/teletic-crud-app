package com.teletic.test_crud.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.zalando.problem.jackson.ProblemModule
import org.zalando.problem.violations.ConstraintViolationProblemModule

@Configuration
class ProblemConfig : WebMvcConfigurer {

    @Bean
    fun problemModule(): ProblemModule {
        return ProblemModule().withStackTraces(false)
    }

    @Bean
    fun constraintViolationProblemModule(): ConstraintViolationProblemModule {
        return ConstraintViolationProblemModule()
    }
}