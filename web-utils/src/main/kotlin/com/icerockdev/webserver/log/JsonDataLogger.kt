/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.icerockdev.api.AbstractResponse
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.http.content.MultiPartData
import io.ktor.request.ApplicationReceivePipeline
import io.ktor.request.ApplicationReceiveRequest
import io.ktor.response.ApplicationSendPipeline
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.application
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import io.ktor.util.toByteArray
import io.ktor.utils.io.ByteReadChannel
import kotlin.reflect.jvm.jvmErasure

class JsonDataLogger(configure: Configuration.() -> Unit) {

    private val mapper: ObjectMapper
    private val configuration: Configuration
    val loggerDataKey = AttributeKey<JsonLoggerData>("JsonLoggerData")

    /**
     * Configuration type for [JsonDataLogger] feature
     */
    class Configuration {
        var mapperConfiguration: ObjectMapper.() -> Unit = {}
        var loggingConfiguration: LoggingConfiguration = LoggingConfiguration(
            responseTypes = listOf(AbstractResponse::class)
        )

        fun isAcceptResponse(response: Any): Boolean {
            return loggingConfiguration.responseTypes.any { type ->
                type.java.isAssignableFrom(response.javaClass)
            }
        }
    }

    init {
        configuration = Configuration().apply(configure)
        mapper = jacksonObjectMapper()
        mapper.apply(configuration.mapperConfiguration)
    }

    fun interceptPipeline(pipeline: ApplicationCallPipeline) {
        pipeline.receivePipeline.insertPhaseBefore(ApplicationReceivePipeline.Before, RequestLoggingPhase)
        // Raw request data intercept
        pipeline.receivePipeline.intercept(RequestLoggingPhase) { request ->
            if (request.typeInfo.jvmErasure.java == MultiPartData::class.java) {
                return@intercept
            }

            val byteArray = when (val requestValue = request.value) {
                is ByteReadChannel -> requestValue.toByteArray()
                else -> null
            }

            setLoggerData(call, getLoggerData(call).copy(requestBody = byteArray?.let { String(it) }))
            val incomingContent = byteArray?.let { ByteReadChannel(it) } ?: request.value
            proceedWith(ApplicationReceiveRequest(request.typeInfo, incomingContent, reusableValue = true))
        }

        // Response data intercept
        pipeline.sendPipeline.insertPhaseBefore(ApplicationSendPipeline.Render, ResponseLoggingPhase)
        pipeline.sendPipeline.intercept(ResponseLoggingPhase) { subject ->
            proceed()
            if (configuration.isAcceptResponse(subject)) {
                setResponseBody(call, subject)
            }
        }
    }

    fun setResponseBody(call: ApplicationCall, responseBody: Any) {
        setLoggerData(call, getLoggerData(call).copy(responseBody = mapper.writeValueAsString(responseBody)))
    }

    private fun getLoggerData(call: ApplicationCall): JsonLoggerData {
        return call.attributes.getOrNull(loggerDataKey) ?: JsonLoggerData()
    }

    private fun setLoggerData(call: ApplicationCall, newJsonLoggerData: JsonLoggerData) {
        call.attributes.put(loggerDataKey, newJsonLoggerData)
    }

    /**
     * Implementation of an [ApplicationFeature] for the [JsonDataLogger]
     */
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, JsonDataLogger> {
        override val key: AttributeKey<JsonDataLogger> = AttributeKey("JsonDataLogger")

        val RequestLoggingPhase = PipelinePhase("RequestLogging")
        val ResponseLoggingPhase = PipelinePhase("ResponseLogging")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): JsonDataLogger {
            return JsonDataLogger(configure)
        }
    }

    data class JsonLoggerData(
        val requestBody: String? = null,
        val responseBody: String? = null
    )
}

fun Route.jsonLogger(build: Route.() -> Unit): Route {
    val route = createChild(JsonDataLoggerRouteSelector())
    application.feature(JsonDataLogger).interceptPipeline(route)
    route.build()
    return route
}

class JsonDataLoggerRouteSelector : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "(json data log)"
}
