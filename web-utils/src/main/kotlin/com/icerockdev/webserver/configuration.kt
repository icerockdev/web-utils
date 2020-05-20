/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.icerockdev.api.ErrorResponse
import com.icerockdev.api.Response
import com.icerockdev.exception.ServerErrorException
import com.icerockdev.exception.UserException
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.util.*

fun getStatusConfiguration(): StatusPages.Configuration.() -> Unit {

    return {
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
            call.respond(
                HttpStatusCode(cause.status, cause.message.toString()),
                cause.getErrorResponse()
            )
        }
        exception<Throwable> { cause ->
            val error = ErrorResponse().also {
                it.status = HttpStatusCode.InternalServerError.value
                it.message = cause.message.toString()
            }
            call.respond(
                HttpStatusCode.InternalServerError,
                error
            )
        }
    }
}

fun applyDefaultCORS(): CORS.Configuration.() -> Unit {
    return {
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
}

fun applyDefaultLogging(configure: CallLogging.Configuration.() -> Unit = {}): CallLogging.Configuration.() -> Unit {
    return {
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
        apply(configure)
    }
}

private fun entriesToString(entries: Set<Map.Entry<String, List<String>>>): String {
    return entries.joinToString(separator = "\n", transform = { String.format("%s: %s", it.key, it.value.first()) })
}


fun getObjectMapper(configure: ObjectMapper.() -> Unit = {}): ObjectMapper.() -> Unit {
    return {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        dateFormat = StdDateFormat()
        apply(configure)
        registerKotlinModule()
    }
}

fun getMonitoringPipeline(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit {
    val logger = LoggerFactory.getLogger(Application::class.java)
    return {
        try {
            proceed()
        } catch (e: UserException) {
            call.respond(
                HttpStatusCode.fromValue(e.status),
                e.getErrorResponse()
            )
            if (e is ServerErrorException) {
                logger.error(e.localizedMessage, e)
            }
            proceed()
        } catch (e: Throwable) {
            call.respond(
                HttpStatusCode.InternalServerError,
                object : Response(
                    status = HttpStatusCode.InternalServerError.value,
                    message = e.message ?: "",
                    isSuccess = false
                ) {}
            )
            proceed()
            logger.error(e.localizedMessage, e)
        }
    }
}

fun getCallConfiguration(): CallId.Configuration.() -> Unit {
    return {
        generate { UUID.randomUUID().toString() }
        header("X-Request-ID")
    }
}
