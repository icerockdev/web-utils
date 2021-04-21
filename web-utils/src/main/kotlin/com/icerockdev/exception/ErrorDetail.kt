/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

data class ErrorDetail(
    val message: String,
    val code: Int = 0,
    val field: String? = null,
)
