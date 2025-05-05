package com.teletic.test_crud.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory
import org.springframework.test.context.MergedContextConfiguration

class SqlTestContainersSpringContextCustomizerFactory : ContextCustomizerFactory {
    private val log = LoggerFactory.getLogger(SqlTestContainersSpringContextCustomizerFactory::class.java)

    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: List<ContextConfigurationAttributes>
    ): ContextCustomizer {
        return object : ContextCustomizer {
            override fun customizeContext(
                context: ConfigurableApplicationContext,
                mergedConfig: MergedContextConfiguration
            ) {
                val beanFactory = context.beanFactory
                var testValues = TestPropertyValues.empty()
                val sqlAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL::class.java)

                if (sqlAnnotation != null) {
                    log.debug("detected the EmbeddedSQL annotation on class {}", testClass.name)
                    log.info("Warming up the sql database")

                    if (prodTestContainer == null) {
                        try {
                            val containerClass = Class.forName(
                                this.javaClass.`package`.name + ".PostgreSqlTestContainer"
                            ) as Class<out SqlTestContainer>

                            prodTestContainer = beanFactory.createBean(containerClass)
                            beanFactory.registerSingleton(containerClass.name, prodTestContainer!!)
                        } catch (e: ClassNotFoundException) {
                            throw RuntimeException(e)
                        }
                    }

                    testValues = testValues.and(
                        "spring.datasource.url=" + prodTestContainer!!.getTestContainer().jdbcUrl,
                        "spring.datasource.username=" + prodTestContainer!!.getTestContainer().username,
                        "spring.datasource.password=" + prodTestContainer!!.getTestContainer().password
                    )
                }
                testValues.applyTo(context)
            }

            override fun hashCode(): Int {
                return SqlTestContainer::class.java.name.hashCode()
            }

            override fun equals(other: Any?): Boolean {
                return this.hashCode() == other?.hashCode()
            }
        }
    }

    companion object {
        private var prodTestContainer: SqlTestContainer? = null
    }
}
