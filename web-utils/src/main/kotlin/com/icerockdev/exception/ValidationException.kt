/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

import com.icerockdev.api.Request
import io.ktor.http.HttpStatusCode
import javax.validation.ConstraintViolation

class ValidationException(message: String = HttpStatusCode.UnprocessableEntity.description) :
    ExtUserException(HttpStatusCode.UnprocessableEntity.value, message) {

    constructor(constraintViolationList: Set<ConstraintViolation<Request>>) : this() {
        val errorList = mutableListOf<ErrorDetail>()
        for (constraintViolation in constraintViolationList) {
            errorList.add(
                ErrorDetail(
                    message = constraintViolation.message,
                    field = constraintViolation.propertyPath?.lastOrNull()?.name
                )
            )
        }
        setErrors(errorList)
    }

    constructor(errorList: List<ErrorDetail>) : this() {
        setErrors(errorList)
    }
}
