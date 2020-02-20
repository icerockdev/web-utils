/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.icerockdev.webserver.tools.DateTimeUtil

open class ResponseList(
    @JsonProperty("data")
    var dataList: List<Any> = listOf(),
    status: Int = 200,
    message: String = "",
    timestamp: Long = DateTimeUtil.getTimestamp(),
    isSuccess: Boolean = true
) : AbstractResponse(status, message, timestamp, isSuccess)
