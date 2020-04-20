/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

import com.icerockdev.api.ErrorResponse

abstract class UserException(
    val status: Int,
    message: String
) : Throwable(message) {

    open fun getErrorResponse(): ErrorResponse {
        return ErrorResponse().also {
            it.status = this.status
            it.message = this.message ?: ""
            it.success = false
        }
    }
}