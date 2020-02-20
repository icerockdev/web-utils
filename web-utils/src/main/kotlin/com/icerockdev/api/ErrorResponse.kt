/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import javax.validation.ConstraintViolation

class ErrorResponse() : ResponseList() {
    data class ErrorDetail(
        val message: String,
        val code: Int = 0
    )

    constructor(constraintViolationList: Set<ConstraintViolation<Request>>) : this() {
        val list = mutableListOf<ErrorDetail>()
        for (constraintViolation in constraintViolationList) {
            list.add(ErrorDetail(message = constraintViolation.message))
        }
        setValidationParams(list)
    }

    private fun setValidationParams(list: List<ErrorDetail>) {
        this.status = 422
        this.isSuccess = false
        this.message = "Validation Error"
        this.dataList = list
    }

    constructor(errorList: List<ErrorDetail>) : this() {
        setValidationParams(errorList)
    }

}