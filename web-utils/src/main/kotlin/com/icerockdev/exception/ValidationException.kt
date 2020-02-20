/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

import com.icerockdev.api.Request
import javax.validation.ConstraintViolation

class ValidationException(message: String = "Validation Error") : ExtUserException(422, message) {

    constructor(constraintViolationList: Set<ConstraintViolation<Request>>) : this() {
        val errorList = mutableListOf<ErrorDetail>()
        for (constraintViolation in constraintViolationList) {
            errorList.add(ErrorDetail(message = constraintViolation.message))
        }
        setErrors(errorList)
    }

    constructor(errorList: List<ErrorDetail>) : this() {
        setErrors(errorList)
    }
}
