/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.icerockdev.api.ErrorResponse
import com.icerockdev.exception.UserException
import com.icerockdev.webserver.log.ApplicationCallLogging
import com.icerockdev.webserver.log.JsonDataLogger
import com.icerockdev.webserver.log.LoggingHelper
import com.icerockdev.webserver.log.callIdMdc
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.pluginOrNull
import io.ktor.server.plugins.callid.CallIdConfig
import io.ktor.server.plugins.cors.CORSConfig
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.event.Level

fun StatusPagesConfig.applyDefaultStatusConfiguration(
    logger: Logger,
    mapper: ObjectMapper = jacksonObjectMapper().apply {
        applyDefaultConfiguration()
    }
) {
    status(HttpStatusCode.NotFound) { call, status ->
        val error = ErrorResponse().also {
            it.status = status.value
            it.message = "Route not found"
            it.success = false
        }
        call.application.pluginOrNull(JsonDataLogger)?.setResponseBody(call, error)
        call.respondText(mapper.writeValueAsString(error), ContentType.Application.Json, status)
    }
    status(HttpStatusCode.Unauthorized) { status ->
        val error = ErrorResponse().also {
            it.status = status.value
            it.message = "Unauthorized"
            it.success = false
        }
        call.respond(status, error)
    }
    exception<UserException> { call, cause ->
        val response = cause.getErrorResponse()
        call.application.pluginOrNull(JsonDataLogger)?.setResponseBody(call, response)
        call.respond(HttpStatusCode(cause.status, cause.message.toString()), response)
        logger.error(cause.localizedMessage, cause)
    }
    exception<Throwable> { call, cause ->
        val errorResponse = ErrorResponse().also {
            it.status = HttpStatusCode.InternalServerError.value
            it.message = cause.message.toString()
        }

        call.application.pluginOrNull(JsonDataLogger)?.setResponseBody(call, errorResponse)
        call.respond(HttpStatusCode.InternalServerError, errorResponse)
        logger.error(cause.localizedMessage, cause)
    }
}

fun CORSConfig.applyDefaultCORS() {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)
    allowHeader(HttpHeaders.Authorization)
    allowHeader("X-Total-Count")
    exposeHeader("X-Total-Count")
    allowCredentials = true
    allowNonSimpleContentTypes = true
}

fun ApplicationCallLogging.Configuration.applyDefaultLogging(
    secretFieldList: List<String> = getDefaultSecretFieldList(),
    userExtractor: (ApplicationCall) -> String? = { null }
) {
    level = Level.TRACE
    callIdMdc(Constants.LOG_FIELD_TRACE_UUID)
    mdc(Constants.LOG_FIELD_ENV) {
        System.getProperty("env", Environment.LOCAL.value)
    }
    mdc(Constants.LOG_FIELD_HEADERS) { call: ApplicationCall ->
        LoggingHelper.entriesToJsonString(call.request.headers.entries())?.let {
            LoggingHelper.replaceSecretFieldsValueInJsonString(it, secretFieldList)
        }
    }
    mdc(Constants.LOG_FIELD_QUERY_PARAMETERS) { call: ApplicationCall ->
        LoggingHelper.entriesToJsonString(call.request.queryParameters.entries())?.let {
            LoggingHelper.replaceSecretFieldsValueInJsonString(it, secretFieldList)
        }
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
        call.application.pluginOrNull(JsonDataLogger)?.let {
            call.attributes.getOrNull(key = it.loggerDataKey)?.requestBody?.let { requestBody ->
                LoggingHelper.replaceSecretFieldsValueInJsonString(requestBody, secretFieldList)
            }
        }
    }
    mdc(Constants.LOG_FIELD_RESPONSE_BODY) { call: ApplicationCall ->
        call.application.pluginOrNull(JsonDataLogger)?.let {
            call.attributes.getOrNull(key = it.loggerDataKey)?.responseBody
        }
    }
    mdc(Constants.LOG_FIELD_USER_ID) { call: ApplicationCall ->
        userExtractor(call)
    }
}

internal fun getDefaultSecretFieldList(): List<String> {
    return listOf("password", "token", "authorization")
}


fun CallIdConfig.applyCallConfiguration() {
    generate { UUID.randomUUID().toString() }
    header("X-Request-ID")
}
