package com.teletic.test_crud.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status
import java.net.URI

class BadRequestAlertException(
    private val entityName: String,
    private val errorKey: String,
    defaultMessage: String
) : AbstractThrowableProblem(
    DEFAULT_TYPE,
    "Bad request",
    Status.BAD_REQUEST,
    defaultMessage,
    null,
    null,
    getAlertParameters(entityName, errorKey)
) {
    override fun getCause(): Exceptional? = super.cause

    companion object {
        private val DEFAULT_TYPE = URI.create("https://teletic.com/problem/bad-request")

        private fun getAlertParameters(entityName: String, errorKey: String): Map<String, Any> {
            return mapOf(
                "message" to "error.$errorKey",
                "params" to entityName,
                "entityName" to entityName,
                "errorKey" to errorKey
            )
        }
    }
}