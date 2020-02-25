/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.fasterxml.jackson.databind.ObjectMapper
import com.icerockdev.api.ErrorResponse
import com.icerockdev.api.Request
import com.icerockdev.exception.ErrorDetail
import com.icerockdev.exception.UserException
import com.icerockdev.i18n.I18N
import com.icerockdev.validation.DateFormat
import com.icerockdev.validation.InIntArray
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.util.*
import javax.validation.constraints.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToolsTest {

    private val mapper: ObjectMapper = ObjectMapper()

    class TestClass(
        @field:NotNull(message = "Name is required")
        var name: String? = "Mike",
        @field:NotNull(message = "Title is required")
        var title: String? = "Mrs",
        @field:Size(min = 2, max = 14, message = "Invalid size of text")
        var text: String = "Test",
        @field:Max(14)
        @field:Min(2)
        @field:InIntArray(value = [10, 20], message = "Should it be 10 or 20")
        var age: Int = 10,
        @field:Email(message = "Invalid email")
        var email: String = "test@test.er",
        @field:DateFormat(message = "Date must be in YYYY-MM-DD format", pattern = "YYYY-MM-DD")
        @field:NotNull(message = "Date is required")
        var date: String = "2020-02-25"
    ) : Request()

    @Test
    fun successValidationRulesTest() {

        val testObj = TestClass()

        val errors = testObj.validate()

        assertEquals(0, errors.size)
        assertTrue(testObj.isValid())
    }

    @Test
    fun failedValidationRulesTest() {

        val testObj = TestClass()

        testObj.name = null
        testObj.age = 21
        testObj.email = "efwfwefewfwe"
        testObj.date = "Invalid date"

        val errors = testObj.validate()

        assertEquals(5, errors.size)
        assertFalse(testObj.isValid())
    }

    @Test
    fun errorResponseTest() {

        val testObj = TestClass()

        testObj.name = null
        testObj.age = 21
        testObj.email = "efwfwefewfwe"
        testObj.date = "Invalid date"

        val errorsResponse = ErrorResponse(testObj.validate())
        errorsResponse.timestamp = 1566554901677

        val expectedErrors = arrayOf(
            ErrorDetail(message="Should it be 10 or 20", code=0),
            ErrorDetail(message="Date must be in YYYY-MM-DD format", code=0),
            ErrorDetail(message="Name is required", code=0),
            ErrorDetail(message="Invalid email", code=0),
            ErrorDetail(message="должно быть меньше или равно 14", code=0)
        ).sortedArrayWith(compareBy {it.message})

        val actualErrors = errorsResponse.dataList.map {
                error -> error as ErrorDetail
        }.toTypedArray().sortedArrayWith(compareBy {it.message})

        assertEquals(5, errorsResponse.dataList.size)
        assertEquals(422, errorsResponse.status)
        assertEquals("Validation Error", errorsResponse.message)
        assertEquals(false, errorsResponse.isSuccess)
        assertArrayEquals(expectedErrors, actualErrors)
    }

    @Test
    fun customValidatorTest() {

        val testObj = TestClass()
        testObj.age = 12
        val errorsResponse = ErrorResponse(testObj.validate())

        errorsResponse.timestamp = 1566554901677

        val expectedErrors = arrayOf(
            ErrorDetail(message="Should it be 10 or 20", code=0)
        ).sortedArrayWith(compareBy {it.message})

        val actualErrors = errorsResponse.dataList.map {
                error -> error as ErrorDetail
        }.toTypedArray().sortedArrayWith(compareBy {it.message})

        assertEquals(1, errorsResponse.dataList.size)
        assertEquals(422, errorsResponse.status)
        assertEquals("Validation Error", errorsResponse.message)
        assertEquals(false, errorsResponse.isSuccess)
        assertArrayEquals(expectedErrors, actualErrors)
    }

    @Test
    fun userExceptionTest() {

        class CustomException(status: Int, message: String) : UserException(status, message)

        val result = CustomException(403, "UserException").getErrorResponse()
        result.timestamp = 1566554901677

        assertEquals(0, result.dataList.size)
        assertEquals(403, result.status)
        assertEquals("UserException", result.message)
        assertEquals(false, result.isSuccess)
    }

    @Test
    fun i18nTest() {
        val i18n = I18N(
            locale = Locale("fr", "FR"),
            defaultCategory = "compile.messages"
        )
        val i18nDefault = I18N(
            locale = Locale("en", "US"),
            defaultCategory = "compile.messages"
        )

        assertEquals("Euro", i18n.t("CurrencyCode"))
        assertEquals("France", i18n.t("CountryName"))

        assertEquals("USD", i18nDefault.t("CurrencyCode"))
        assertEquals("USA", i18nDefault.t("CountryName"))
    }
}
