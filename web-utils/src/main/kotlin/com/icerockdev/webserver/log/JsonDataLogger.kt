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
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.request.ApplicationReceivePipeline
import io.ktor.response.ApplicationSendPipeline
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.MDC
import java.io.IOException


@KtorExperimentalAPI
class JsonDataLogger() {

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
    }


    /**
     * Implementation of an [ApplicationFeature] for the [JsonDataLogger]
     */
    companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, Configuration, JsonDataLogger> {
        override val key: AttributeKey<JsonDataLogger> = AttributeKey("JsonDataLogger")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): JsonDataLogger {
            val configuration = Configuration().apply(configure)

            // configure mapper for hide secret values
            val mapper = jacksonObjectMapper()
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

            val feature = JsonDataLogger()
            val loggingPhase = PipelinePhase("RequestLogging")

            // response data intercept
            pipeline.sendPipeline.insertPhaseBefore(ApplicationSendPipeline.Render, loggingPhase)
            pipeline.sendPipeline.intercept(loggingPhase) { subject ->
                if (subject is AbstractResponse) {
                    MDC.put(configuration.responseBodyName, mapper.writeValueAsString(subject))
                }
            }

            // Received data intercept
            pipeline.receivePipeline.insertPhaseBefore(ApplicationReceivePipeline.After, loggingPhase)
            pipeline.receivePipeline.intercept(loggingPhase) { request ->
                val requestValue = request.value
                if (requestValue is Request) {
                    MDC.put(configuration.requestBodyName, mapper.writeValueAsString(requestValue))
                }
            }

            pipeline.insertPhaseBefore(ApplicationCallPipeline.Monitoring, loggingPhase)
            pipeline.intercept(loggingPhase) {
                proceed()
                MDC.put(configuration.responseStatusCodeName, call.response.status()?.value.toString())
                MDC.put(configuration.appEnvName, System.getProperty(configuration.systemEnvKey, Environment.LOCAL.value))
            }

            return feature
        }
    }
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