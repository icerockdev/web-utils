/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.fasterxml.jackson.databind.ObjectMapper
import com.icerockdev.api.ErrorResponse
import com.icerockdev.api.Request
import com.icerockdev.exception.ErrorDetail
import com.icerockdev.exception.UserException
import com.icerockdev.i18n.I18N
import com.icerockdev.validation.*
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.util.*
import javax.validation.constraints.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

enum class Status(val value: Int) : IAvailableIntByEnum {
    STATUS_ACTIVE(30),
    STATUS_BLOCKED(40);

    override fun getAvailableIntList(listName: String): List<Int> {
        return when (listName) {
            "all" -> arrayListOf(STATUS_ACTIVE.value, STATUS_BLOCKED.value)
            else -> {
                emptyList()
            }
        }
    }
}

class ToolsTest {

    private val mapper: ObjectMapper = ObjectMapper()

    @FieldMatch(message = "The password fields must match",first = "password", second = "passwordRepeat")
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
        @field:InIntListByEnum(IAvailableIntByEnum = Status::class, message = "Should it be 30 or 40")
        var status: Int = 30,
        @field:StrictEmail(message = "Invalid email")
        var email: String = "test@test.er",
        @field:DateFormat(message = "Date must be in YYYY-MM-DD format", pattern = "YYYY-MM-DD")
        @field:NotNull(message = "Date is required")
        var date: String = "2020-02-25",
        @field:NotNull(message = "Password is required")
        var password: String = "123456",
        @field:NotNull(message = "Password repeat is required")
        var passwordRepeat: String = "123456"
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
        testObj.status = 10
        testObj.email = "test@test"
        testObj.date = "Invalid date"
        testObj.passwordRepeat = "123457"

        val errors = testObj.validate()

        assertEquals(7, errors.size)
        assertFalse(testObj.isValid())
    }

    @Test
    fun errorResponseTest() {

        val testObj = TestClass()

        testObj.name = null
        testObj.age = 21
        testObj.status = 10
        testObj.email = "test@test"
        testObj.date = "Invalid date"
        testObj.passwordRepeat = "123457"

        val errorsResponse = ErrorResponse(testObj.validate())
        errorsResponse.timestamp = 1566554901677

        val expectedErrors = arrayOf(
            ErrorDetail(message="Should it be 10 or 20", code=0),
            ErrorDetail(message="Should it be 30 or 40", code=0),
            ErrorDetail(message="Date must be in YYYY-MM-DD format", code=0),
            ErrorDetail(message="Name is required", code=0),
            ErrorDetail(message="email определен в неверном формате", code=0),
            ErrorDetail(message="должно быть меньше или равно 14", code=0),
            ErrorDetail(message="The password fields must match", code=0)
        ).sortedArrayWith(compareBy {it.message})

        val actualErrors = errorsResponse.dataList.map {
                error -> error as ErrorDetail
        }.toTypedArray().sortedArrayWith(compareBy {it.message})

        actualErrors.forEach { e ->
            println(e.message)
        }

        assertEquals(7, errorsResponse.dataList.size)
        assertEquals(422, errorsResponse.status)
        assertEquals("Validation Error", errorsResponse.message)
        assertEquals(false, errorsResponse.isSuccess)
        assertEquals(errorsResponse.dataList.count(), errorsResponse.totalCount)
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
}
