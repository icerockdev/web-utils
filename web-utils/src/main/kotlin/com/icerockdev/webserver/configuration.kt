/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.icerockdev.api.ErrorResponse
import com.icerockdev.exception.UserException
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.CORS
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.features.callIdMdc
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.event.Level
import java.util.UUID

fun StatusPages.Configuration.applyStatusConfiguration() {
    val logger = LoggerFactory.getLogger(Application::class.java)
    val mapper = jacksonObjectMapper().apply {
        setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        })
        applyObjectMapper()
    }
    status(HttpStatusCode.NotFound) { status ->
        val error = ErrorResponse().also {
            it.status = status.value
            it.message = "Route not found"
        }
        call.respond(
            status,
            error
        )
    }
    status(HttpStatusCode.Unauthorized) { status ->
        val error = ErrorResponse().also {
            it.status = status.value
            it.message = "Unauthorized"
            it.success = false
        }
        call.respond(
            status,
            error
        )
    }
    exception<UserException> { cause ->
        MDC.put(Constants.LOG_FIELD_RESPONSE_BODY, mapper.writeValueAsString(cause.getErrorResponse()))
        call.respond(
            HttpStatusCode(cause.status, cause.message.toString()),
            cause.getErrorResponse()
        )
        logger.error(cause.localizedMessage, cause)
    }
    exception<Throwable> { cause ->
        val errorResponse = ErrorResponse().also {
            it.status = HttpStatusCode.InternalServerError.value
            it.message = cause.message.toString()
        }
        MDC.put(Constants.LOG_FIELD_RESPONSE_BODY, mapper.writeValueAsString(errorResponse))
        call.respond(
            HttpStatusCode.InternalServerError,
            errorResponse
        )
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

fun CallLogging.Configuration.applyDefaultLogging() {
    level = Level.TRACE
    callIdMdc(Constants.LOG_FIELD_TRACE_UUID)
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
}

private fun entriesToString(entries: Set<Map.Entry<String, List<String>>>): String {
    return entries.joinToString(separator = "\n", transform = { String.format("%s: %s", it.key, it.value.first()) })
}


fun ObjectMapper.applyObjectMapper() {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    dateFormat = StdDateFormat()
    registerKotlinModule()
}

fun CallId.Configuration.applyCallConfiguration() {
    generate { UUID.randomUUID().toString() }
    header("X-Request-ID")
}
