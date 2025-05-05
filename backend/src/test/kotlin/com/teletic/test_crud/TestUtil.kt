package com.teletic.test_crud.web.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.format.support.DefaultFormattingConversionService
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass

/**
 * Utility class for testing REST controllers.
 */
object TestUtil {
    /** MediaType for JSON */
    val APPLICATION_JSON_UTF8: MediaType = MediaType.APPLICATION_JSON

    /**
     * Convert an object to JSON byte array.
     *
     * @param object the object to convert
     * @return the JSON byte array
     * @throws IOException if conversion fails
     */
    @Throws(IOException::class)
    fun convertObjectToJsonBytes(`object`: Any): ByteArray {
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return mapper.writeValueAsBytes(`object`)
    }

    /**
     * Create a FormattingConversionService which uses ISO date format, instead of the localized one.
     * @return the FormattingConversionService
     */
    fun createFormattingConversionService(): FormattingConversionService {
        val dateTimeFormatterRegistrar = DateTimeFormatterRegistrar()
        dateTimeFormatterRegistrar.setUseIsoFormat(true)
        val conversionService = DefaultFormattingConversionService()
        dateTimeFormatterRegistrar.registerFormatters(conversionService)
        return conversionService
    }

    /**
     * Creates a matcher that matches when the examined object represents the same instant as the reference datetime.
     * @param expected the reference datetime against which the examined datetime is checked
     */
    fun sameInstant(expected: ZonedDateTime): ZonedDateTimeMatcher {
        return ZonedDateTimeMatcher(expected)
    }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference datetime.
     */
    class ZonedDateTimeMatcher(private val date: ZonedDateTime) : TypeSafeDiagnosingMatcher<String>() {
        override fun matchesSafely(item: String, mismatchDescription: Description): Boolean {
            try {
                if (!ZonedDateTime.parse(item).isEqual(date)) {
                    mismatchDescription.appendText("was ").appendValue(item)
                    return false
                }
                return true
            } catch (e: DateTimeParseException) {
                mismatchDescription.appendText("was ").appendValue(item)
                    .appendText(", which could not be parsed as a ZonedDateTime")
                return false
            }
        }

        override fun describeTo(description: Description) {
            description.appendText("a String representing the same Instant as ").appendValue(date)
        }
    }

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    fun <T: Any> equalsVerifier(clazz: KClass<T>) {
        val domainObject1 = clazz.java.getDeclaredConstructor().newInstance()
        val domainObject2 = clazz.java.getDeclaredConstructor().newInstance()

        assertNotEquals(domainObject1, null)
        assertNotEquals(domainObject1, Any())

        assertEquals(domainObject1, domainObject1)
        assertEquals(domainObject1.hashCode(), domainObject1.hashCode())

        // Test with an instance of another class
        assertNotEquals(domainObject1, domainObject2)

        // Test with equal field values
        domainObject1.javaClass.declaredFields.forEach { field ->
            if (field.name != "id") {
                field.isAccessible = true
                field.set(domainObject1, "field")
                field.set(domainObject2, "field")
            }
        }

        assertEquals(domainObject1, domainObject2)
        assertEquals(domainObject1.hashCode(), domainObject2.hashCode())

        // Test with different field values
        domainObject1.javaClass.declaredFields.forEach { field ->
            if (field.name != "id") {
                field.isAccessible = true
                field.set(domainObject1, "field1")
                field.set(domainObject2, "field2")
            }
        }

        assertNotEquals(domainObject1, domainObject2)
        assertNotEquals(domainObject1.hashCode(), domainObject2.hashCode())
    }

    private fun assertEquals(a: Any, b: Any) {
        assert(a == b) { "$a should equal $b" }
    }

    private fun assertNotEquals(a: Any, b: Any?) {
        assert(a != b) { "$a should not equal $b" }
    }
}
