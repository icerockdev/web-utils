/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException

class BadRequestException(message: String = "Bad Request") : ExtUserException(400, message) {
    constructor(exception: MissingKotlinParameterException) : this() {
        val errorList = mutableListOf<ErrorDetail>()
        val fieldName = exception.path.joinToString(separator = ".") { it.fieldName ?: "[]" }
        errorList.add(ErrorDetail(message = "Property $fieldName are required"))
        setErrors(errorList)
    }

    constructor(errorList: List<ErrorDetail>) : this() {
        setErrors(errorList)
    }
}
