/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.icerockdev.exception.ErrorDetail
import javax.validation.ConstraintViolation

class ErrorResponse() : ResponseList() {
    constructor(constraintViolationList: Set<ConstraintViolation<Request>>) : this() {
        val list = mutableListOf<ErrorDetail>()
        for (constraintViolation in constraintViolationList) {
            list.add(ErrorDetail(message = constraintViolation.message))
        }
        setValidationParams(list)
        totalCount = list.count()
    }

    private fun setValidationParams(list: List<ErrorDetail>) {
        this.status = 422
        this.success = false
        this.message = "Validation Error"
        this.data = list
    }

    constructor(errorList: List<ErrorDetail>) : this() {
        setValidationParams(errorList)
        totalCount = errorList.count()
    }

}
