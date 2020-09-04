/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver.log

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import com.icerockdev.webserver.Constants
import com.icerockdev.webserver.Environment
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.application.feature
import io.ktor.request.ApplicationReceivePipeline
import io.ktor.response.ApplicationSendPipeline
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.routing.application
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.MDC

@KtorExperimentalAPI
class JsonDataLogger(configure: Configuration.() -> Unit) {

    private val mapper: ObjectMapper
    private val configuration: Configuration

    /**
     * Configuration type for [JsonDataLogger] feature
     */
    class Configuration {
        var mapperConfiguration: ObjectMapper.() -> Unit = {}
        var responseBodyName: String = Constants.LOG_FIELD_RESPONSE_BODY
        var requestBodyName: String = Constants.LOG_FIELD_REQUEST_BODY
        var responseStatusCodeName: String = Constants.LOG_FIELD_STATUS_CODE
        var appEnvName: String = Constants.LOG_FIELD_ENV
        var systemEnvKey: String = "env"
        var loggingConfiguration: LoggingConfiguration = LoggingConfiguration(
            requestTypes = listOf(Request::class),
            responseTypes = listOf(AbstractResponse::class)
        )
    }

    init {
        configuration = Configuration().apply(configure)
        mapper = jacksonObjectMapper()
        mapper.apply(configuration.mapperConfiguration)
    }

    fun interceptPipeline(pipeline: ApplicationCallPipeline) {
        // Response status code & app env intercept
        pipeline.insertPhaseBefore(ApplicationCallPipeline.Monitoring, MonitoringLoggingPhase)
        pipeline.intercept(MonitoringLoggingPhase) {
            proceed()
            MDC.put(configuration.responseStatusCodeName, call.response.status()?.value.toString())
            MDC.put(configuration.appEnvName, System.getProperty(configuration.systemEnvKey, Environment.LOCAL.value))
        }

        // Request data intercept
        pipeline.receivePipeline.insertPhaseBefore(ApplicationReceivePipeline.After, RequestLoggingPhase)
        pipeline.receivePipeline.intercept(RequestLoggingPhase) { request ->
            proceed()
            val requestValue = request.value
            configuration.loggingConfiguration.requestTypes.forEach { type ->
                if (type.java.isAssignableFrom(subject.javaClass)) {
                    MDC.put(configuration.requestBodyName, mapper.writeValueAsString(requestValue))
                }
            }
        }

        // Response data intercept
        pipeline.sendPipeline.insertPhaseBefore(ApplicationSendPipeline.Render, ResponseLoggingPhase)
        pipeline.sendPipeline.intercept(ResponseLoggingPhase) { subject ->
            proceed()
            configuration.loggingConfiguration.responseTypes.forEach { type ->
                if (type.java.isAssignableFrom(subject.javaClass)) {
                    MDC.put(configuration.responseBodyName, mapper.writeValueAsString(subject))
                }
            }
        }
    }

    /**
     * Implementation of an [ApplicationFeature] for the [JsonDataLogger]
     */
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, JsonDataLogger> {
        override val key: AttributeKey<JsonDataLogger> = AttributeKey("JsonDataLogger")

        val RequestLoggingPhase = PipelinePhase("RequestLogging")
        val ResponseLoggingPhase = PipelinePhase("ResponseLogging")
        val MonitoringLoggingPhase = PipelinePhase("MonitoringLogging")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): JsonDataLogger {
            return JsonDataLogger(configure)
        }
    }
}

@KtorExperimentalAPI
fun Route.jsonLogger(build: Route.() -> Unit): Route {
    val route = createChild(JsonDataLoggerRouteSelector())
    application.feature(JsonDataLogger).interceptPipeline(route)
    route.build()
    return route
}

class JsonDataLoggerRouteSelector : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "(json data log)"
}
