/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.icerockdev.webserver.tools.DateTimeUtil

abstract class AbstractResponse(
    var status: Int = 0,
    var message: String = "",
    var timestamp: Long = DateTimeUtil.getTimestamp(),
    var success: Boolean = false
)
