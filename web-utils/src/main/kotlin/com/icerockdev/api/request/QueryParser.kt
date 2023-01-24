/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api.request

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.icerockdev.exception.BadRequestException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase

class QueryParser(val mapper: ObjectMapper, val errorLogging: Boolean) {
    /**
     * Configuration type for [QueryParser] feature
     */
    class Configuration {
        var mapperConfiguration: ObjectMapper.() -> Unit = {}
        var errorLogging = false
    }

    /**
     * Implementation of an [BaseApplicationPlugin] for the [QueryParser]
     */
    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, QueryParser> {
        override val key = queryParserAttributeKey

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): QueryParser {
            val configuration = Configuration().apply(configure)

            // configure mapper for hide secret values
            val mapper = jacksonObjectMapper()
            mapper.apply(configuration.mapperConfiguration)
            val queryParser = QueryParser(mapper = mapper, errorLogging = configuration.errorLogging)

            val queryParserPhase = PipelinePhase("QueryParser")
            // put to attributes link to configuration (intercept body execution)
            pipeline.insertPhaseBefore(ApplicationCallPipeline.Monitoring, queryParserPhase)
            pipeline.intercept(queryParserPhase) {
                val parser = call.attributes.getOrNull(queryParserAttributeKey)
                if (parser == null) {
                    call.attributes.put(queryParserAttributeKey, queryParser)
                }
            }

            return queryParser
        }
    }
}

val queryParserAttributeKey: AttributeKey<QueryParser> = AttributeKey("QueryParser")

@Throws(IllegalArgumentException::class)
inline fun <reified T> ApplicationCall.receiveQuery(): T {
    return receiveQueryOrNull<T>() ?: throw BadRequestException()
}

/**
 * Return null for invalid convert to object
 * Available only case insensitive parsing, duplicate keys present as list.
 * Convert behavior from jackson deserializer
 * Jackson configured by QueryParser feature
 */
inline fun <reified T> ApplicationCall.receiveQueryOrNull(): T? {
    val parser = attributes[queryParserAttributeKey]

    /**
     * parameters create by ParametersBuilder, included StringValuesBuilder with caseInsensitiveName flag
     * As result we have age and Age are identical
     */
    val mappingValues = parameters.entries().associateByTo(LinkedHashMap(), { it.key }, {
        if (it.value.isEmpty()) {
            return@associateByTo null
        }
        if (it.value.size == 1) {
            return@associateByTo it.value.firstOrNull()
        }
        return@associateByTo it.value
    })
    return try {
        parser.mapper.convertValue(mappingValues, jacksonTypeRef<T>()) as T
    } catch (e: IllegalArgumentException) {
        if (parser.errorLogging) {
            application.log.error(e.localizedMessage, e)
        }

        null
    }
}
