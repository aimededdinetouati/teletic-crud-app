package com.teletic.test_crud.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import java.util.*

class PostgreSqlTestContainer : SqlTestContainer {
    private val log = LoggerFactory.getLogger(PostgreSqlTestContainer::class.java)

    private var postgreSQLContainer: PostgreSQLContainer<*>? = null

    override fun destroy() {
        if (postgreSQLContainer != null && postgreSQLContainer!!.isRunning) {
            postgreSQLContainer!!.stop()
        }
    }

    override fun afterPropertiesSet() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine")
                .withDatabaseName("testdb")
        }
        if (!postgreSQLContainer!!.isRunning) {
            postgreSQLContainer!!.start()
        }
    }

    override fun getTestContainer(): JdbcDatabaseContainer<*> {
        return postgreSQLContainer!!
    }
}