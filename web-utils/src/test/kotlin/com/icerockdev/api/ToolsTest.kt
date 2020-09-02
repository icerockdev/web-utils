/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.icerockdev.api.request.NestedTestListItemRequest
import com.icerockdev.api.request.TestRequest
import com.icerockdev.exception.ErrorDetail
import com.icerockdev.exception.UserException
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToolsTest {
    @Test
    fun successValidationRulesTest() {
        val testObj = TestRequest()

        assertTrue(testObj.validate())
        assertEquals(0, testObj.getErrorList().size)
    }

    @Test
    fun failedValidationRulesTest() {
        val testObj = TestRequest()

        testObj.name = null
        testObj.age = 21
        testObj.status = 10
        testObj.mode = "ACTIVE"
        testObj.email = "test@test"
        testObj.date = "Invalid date"
        testObj.passwordRepeat = "123457"
        testObj.nested.list.add(NestedTestListItemRequest(10, "test10"))

        assertFalse(testObj.validate())
        assertEquals(7, testObj.getErrorList().size)
        assertFalse(testObj.validateRecursive())
        assertEquals(8, testObj.getErrorList().size)
    }

    @Test
    fun errorResponseTest() {
        val testObj = TestRequest()

        testObj.name = null
        testObj.age = 21
        testObj.status = 10
        testObj.mode = "ANYTHING"
        testObj.email = "test@test"
        testObj.date = "Invalid date"
        testObj.passwordRepeat = "123457"
        testObj.nested.list.add(NestedTestListItemRequest(10, "test10"))

        testObj.validateRecursive()
        val errorsResponse = ErrorResponse(testObj.getErrorList())
        errorsResponse.timestamp = 1566554901677

        val expectedErrors = arrayOf(
            ErrorDetail(message = "Should it be 10 or 20", code = 0),
            ErrorDetail(message = "Should it be 30 or 40", code = 0),
            ErrorDetail(message = "Should be ACTIVE or PASSIVE", code = 0),
            ErrorDetail(message = "Date must be in YYYY-MM-DD format", code = 0),
            ErrorDetail(message = "Name is required", code = 0),
            ErrorDetail(message = "Invalid email", code = 0),
            ErrorDetail(message = "должно быть не больше 14", code = 0),
            ErrorDetail(message = "The password fields must match", code = 0),
            ErrorDetail(message = "Invalid length of Value", code = 0)
        ).sortedArrayWith(compareBy { it.message })

        val actualErrors = errorsResponse.data.map { error ->
            error as ErrorDetail
        }.toTypedArray().sortedArrayWith(compareBy { it.message })

        actualErrors.forEach { e ->
            println(e.message)
        }

        assertEquals(9, errorsResponse.data.size)
        assertEquals(422, errorsResponse.status)
        assertEquals("Validation Error", errorsResponse.message)
        assertEquals(false, errorsResponse.success)
        assertEquals(errorsResponse.data.count(), errorsResponse.totalCount)
        assertArrayEquals(expectedErrors, actualErrors)
    }

    @Test
    fun customValidatorTest() {
        val testObj = TestRequest()
        testObj.age = 12

        testObj.validateRecursive()
        val errorsResponse = ErrorResponse(testObj.getErrorList())
        errorsResponse.timestamp = 1566554901677

        val expectedErrors = arrayOf(
            ErrorDetail(message = "Should it be 10 or 20", code = 0)
        ).sortedArrayWith(compareBy { it.message })

        val actualErrors = errorsResponse.data.map { error ->
            error as ErrorDetail
        }.toTypedArray().sortedArrayWith(compareBy { it.message })

        assertEquals(1, errorsResponse.data.size)
        assertEquals(422, errorsResponse.status)
        assertEquals("Validation Error", errorsResponse.message)
        assertEquals(false, errorsResponse.success)
        assertArrayEquals(expectedErrors, actualErrors)
    }

    @Test
    fun userExceptionTest() {
        class CustomException(status: Int, message: String) : UserException(status, message)

        val result = CustomException(403, "UserException").getErrorResponse()
        result.timestamp = 1566554901677

        assertEquals(0, result.data.size)
        assertEquals(403, result.status)
        assertEquals("UserException", result.message)
        assertEquals(false, result.success)
    }
}
