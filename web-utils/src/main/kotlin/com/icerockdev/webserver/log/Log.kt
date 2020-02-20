/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver.log

import com.icerockdev.webserver.Constants
import com.icerockdev.webserver.Environment
import io.ktor.application.Application
import io.ktor.http.HttpStatusCode
import io.ktor.util.error
import org.slf4j.LoggerFactory
import org.slf4j.MDC

object Log {
    fun throwableLog(e: Throwable) {
        val logger = LoggerFactory.getLogger(Application::class.java)

        MDC.put(Constants.LOG_FIELD_STATUS_CODE, HttpStatusCode.InternalServerError.value.toString())
        MDC.put(Constants.LOG_FIELD_ENV, System.getProperty("env", Environment.LOCAL.value))

        logger.error(e)
    }
}