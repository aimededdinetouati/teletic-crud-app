package com.teletic.test_crud.web.rest.errors

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.ProblemBuilder
import org.zalando.problem.Status
import org.zalando.problem.spring.web.advice.ProblemHandling
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait
import java.net.URI

@ControllerAdvice
class ExceptionTranslator : ProblemHandling, SecurityAdviceTrait {

    // This is already handled by SecurityAdviceTrait, but we can customize it
    override fun handleAccessDenied(e: AccessDeniedException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = Problem.builder()
            .withType(URI.create("https://teletic.com/problem/access-denied"))
            .withTitle("Access Denied")
            .withStatus(Status.FORBIDDEN)
            .withDetail("You don't have the necessary permission to access this resource")
            .with("message", "error.accessDenied")
            .with("path", request.getNativeRequest(HttpServletRequest::class.java)?.requestURI ?: "unknown")
            .build()

        return ResponseEntity.status(Status.FORBIDDEN.statusCode).body(problem)
    }

    // Other exception handlers...

    // Use this for generic exceptions
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: NativeWebRequest): ResponseEntity<Problem> {
        return create(Status.INTERNAL_SERVER_ERROR, ex, request)
    }
}