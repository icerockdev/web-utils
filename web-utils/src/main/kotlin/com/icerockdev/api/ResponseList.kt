/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.icerockdev.webserver.tools.DateTimeUtil

open class ResponseList(
    open var data: List<Any> = listOf(),
    status: Int = 200,
    message: String = "",
    timestamp: Long = DateTimeUtil.getTimestamp(),
    isSuccess: Boolean = true,
    var totalCount: Int = 0
) : AbstractResponse(status, message, timestamp, isSuccess)
