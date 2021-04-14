/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver

import com.icerockdev.api.ErrorResponse
import com.icerockdev.exception.UserException
import com.icerockdev.webserver.log.ApplicationCallLogging
import com.icerockdev.webserver.log.JsonDataLogger
import com.icerockdev.webserver.log.callIdMdc
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.featureOrNull
import io.ktor.features.CORS
import io.ktor.features.CallId
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.event.Level

fun StatusPages.Configuration.applyDefaultStatusConfiguration(logger: Logger) {
    status(HttpStatusCode.NotFound) { status ->
        val error = ErrorResponse().also {
            it.status = status.value
            it.message = "Route not found"
        }
        call.respond(status, error)
    }
    status(HttpStatusCode.Unauthorized) { status ->
        val error = ErrorResponse().also {
            it.status = status.value
            it.message = "Unauthorized"
            it.success = false
        }
        call.respond(status, error)
    }
    exception<UserException> { cause ->
        val response = cause.getErrorResponse()
        call.application.featureOrNull(JsonDataLogger)?.setResponseBody(call, response)
        call.respond(HttpStatusCode(cause.status, cause.message.toString()), response)
        logger.error(cause.localizedMessage, cause)
    }
    exception<Throwable> { cause ->
        val errorResponse = ErrorResponse().also {
            it.status = HttpStatusCode.InternalServerError.value
            it.message = cause.message.toString()
        }

        call.application.featureOrNull(JsonDataLogger)?.setResponseBody(call, errorResponse)
        call.respond(HttpStatusCode.InternalServerError, errorResponse)
        logger.error(cause.localizedMessage, cause)
    }
}

fun CORS.Configuration.applyDefaultCORS() {
    method(HttpMethod.Options)
    method(HttpMethod.Put)
    method(HttpMethod.Delete)
    method(HttpMethod.Patch)
    header(HttpHeaders.Authorization)
    header("X-Total-Count")
    exposeHeader("X-Total-Count")
    allowCredentials = true
    allowNonSimpleContentTypes = true
}

fun ApplicationCallLogging.Configuration.applyDefaultLogging() {
    level = Level.TRACE
    callIdMdc(Constants.LOG_FIELD_TRACE_UUID)
    mdc(Constants.LOG_FIELD_ENV) {
        System.getProperty("env", Environment.LOCAL.value)
    }
    mdc(Constants.LOG_FIELD_HEADERS) { call: ApplicationCall ->
        entriesToString(call.request.headers.entries())
    }
    mdc(Constants.LOG_FIELD_QUERY_PARAMETERS) { call: ApplicationCall ->
        entriesToString(call.request.queryParameters.entries())
    }
    mdc(Constants.LOG_FIELD_HTTP_METHOD) { call: ApplicationCall ->
        call.request.httpMethod.value
    }
    mdc(Constants.LOG_FIELD_REQUEST_PATH) { call: ApplicationCall ->
        call.request.path()
    }
    mdc(Constants.LOG_FIELD_STATUS_CODE) { call: ApplicationCall ->
        call.response.status()?.value.toString()
    }
    mdc(Constants.LOG_FIELD_REQUEST_BODY) { call: ApplicationCall ->
        call.application.featureOrNull(JsonDataLogger)?.let {
            call.attributes.getOrNull(key = it.loggerDataKey)?.requestBody
        }
    }
    mdc(Constants.LOG_FIELD_RESPONSE_BODY) { call: ApplicationCall ->
        call.application.featureOrNull(JsonDataLogger)?.let {
            call.attributes.getOrNull(key = it.loggerDataKey)?.responseBody
        }
    }
}

fun CallId.Configuration.applyCallConfiguration() {
    generate { UUID.randomUUID().toString() }
    header("X-Request-ID")
}

fun entriesToString(entries: Set<Map.Entry<String, List<String>>>): String {
    return entries.joinToString(separator = "\n") { entry ->
        entry.value.joinToString(separator = "\n") {
            String.format("%s: %s", entry.key, it)
        }
    }
}
