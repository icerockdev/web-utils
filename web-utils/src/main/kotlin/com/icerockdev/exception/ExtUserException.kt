/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

import com.fasterxml.jackson.annotation.JsonProperty
import com.icerockdev.api.ErrorResponse

abstract class ExtUserException(status: Int, message: String) :
    UserException(status, message) {
    @JsonProperty("data")
    open var dataList: List<ErrorDetail> = mutableListOf()

    override fun getErrorResponse(): ErrorResponse {
        return ErrorResponse().also {
            it.status = this.status
            it.message = this.message.toString()
            it.isSuccess = false
            it.dataList = dataList
        }
    }

    protected fun setErrors(list: List<ErrorDetail>) {
        this.dataList = list
    }
}
