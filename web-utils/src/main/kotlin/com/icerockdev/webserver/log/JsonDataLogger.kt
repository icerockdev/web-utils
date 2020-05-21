/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver.log

import com.fasterxml.jackson.annotation.JacksonAnnotation
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import com.icerockdev.webserver.Constants
import com.icerockdev.webserver.Environment
import io.ktor.application.*
import io.ktor.request.ApplicationReceivePipeline
import io.ktor.response.ApplicationSendPipeline
import io.ktor.routing.*
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.MDC
import java.io.IOException


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

        // configure mapper for hide secret values
        mapper = jacksonObjectMapper()
        mapper.apply {
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                indentObjectsWith(DefaultIndenter("  ", "\n"))
            })
            // intercept custom annotation and replace value
            setAnnotationIntrospector(object : JacksonAnnotationIntrospector() {
                override fun findSerializer(annotated: Annotated?): Any? {
                    val ann = annotated?.getAnnotation(JsonSecret::class.java)
                    if (ann != null) {
                        return SecretSerializer::class.java
                    }
                    return super.findSerializer(annotated)
                }
            })
        }
        mapper.apply(configuration.mapperConfiguration)
    }

    fun interceptPipeline(
        pipeline: ApplicationCallPipeline
    ) {
        // response data intercept
        pipeline.sendPipeline.insertPhaseBefore(ApplicationSendPipeline.Render, LoggingPhase)
        pipeline.sendPipeline.intercept(LoggingPhase) { subject ->
            configuration.loggingConfiguration.responseTypes.forEach { type ->
                if (type.isInstance(subject)) {
                    MDC.put(configuration.responseBodyName, mapper.writeValueAsString(subject))
                }
            }
        }

        // Received data intercept
        pipeline.receivePipeline.insertPhaseBefore(ApplicationReceivePipeline.After, LoggingPhase)
        pipeline.receivePipeline.intercept(LoggingPhase) { request ->
            val requestValue = request.value
            configuration.loggingConfiguration.requestTypes.forEach { type ->
                if (type.isInstance(requestValue)) {
                    MDC.put(configuration.requestBodyName, mapper.writeValueAsString(requestValue))
                }
            }
        }

        pipeline.insertPhaseBefore(ApplicationCallPipeline.Monitoring, LoggingPhase)
        pipeline.intercept(LoggingPhase) {
            proceed()
            MDC.put(configuration.responseStatusCodeName, call.response.status()?.value.toString())
            MDC.put(configuration.appEnvName, System.getProperty(configuration.systemEnvKey, Environment.LOCAL.value))
        }
    }

    /**
     * Implementation of an [ApplicationFeature] for the [JsonDataLogger]
     */
    companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, Configuration, JsonDataLogger> {
        override val key: AttributeKey<JsonDataLogger> = AttributeKey("JsonDataLogger")

        val LoggingPhase = PipelinePhase("RequestLogging")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): JsonDataLogger {

            return JsonDataLogger(configure)
        }
    }
}

@KtorExperimentalAPI
fun Route.jsonLogger(
    build: Route.() -> Unit
): Route {
    val route = createChild(JsonDataLoggerRouteSelector())

    application.feature(JsonDataLogger).interceptPipeline(route)
    route.build()
    return route
}

class JsonDataLoggerRouteSelector : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Constant
    }

    override fun toString(): String = "(JsonDataLogger)"
}

@Target(AnnotationTarget.FIELD)
@JacksonAnnotation
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonSecret()

@JacksonStdImpl
class SecretSerializer : StdScalarSerializer<Any?>(String::class.java, false) {

    @Throws(IOException::class)
    override fun serializeWithType(
        value: Any?, gen: JsonGenerator, provider: SerializerProvider,
        typeSer: TypeSerializer
    ) { // no type info, just regular serialization
        gen.writeString("****")
    }

    override fun serialize(value: Any?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen!!.writeString("****")
    }
}
