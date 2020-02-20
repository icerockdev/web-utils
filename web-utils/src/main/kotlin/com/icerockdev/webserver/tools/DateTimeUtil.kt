/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver.tools

class DateTimeUtil {
    companion object {
        fun getTimestamp(): Long {
            return System.currentTimeMillis() / 1000
        }
    }
}