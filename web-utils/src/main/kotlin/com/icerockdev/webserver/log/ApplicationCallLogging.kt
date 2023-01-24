package com.icerockdev.webserver.log

import io.ktor.events.Events
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStarting
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelinePhase
import org.slf4j.Logger
import org.slf4j.MDC
import org.slf4j.event.Level

/**
 * Logs application lifecycle and call events.
 */
class ApplicationCallLogging private constructor(
    private val log: Logger,
    private val monitor: Events,
    private val level: Level,
    private val filters: List<(ApplicationCall) -> Boolean>,
    private val mdcEntries: List<MDCEntry>,
    private val formatCall: (ApplicationCall) -> String
) {

    internal class MDCEntry(val name: String, val provider: (ApplicationCall) -> String?)

    /**
     * Configuration for [ApplicationCallLogging] feature
     */
    class Configuration {
        internal val filters = mutableListOf<(ApplicationCall) -> Boolean>()
        internal val mdcEntries = mutableListOf<MDCEntry>()
        internal var formatCall: (ApplicationCall) -> String = ::defaultFormat

        /**
         * Logging level for [ApplicationCallLogging], default is [Level.TRACE]
         */
        var level: Level = Level.TRACE

        /**
         * Customize [Logger], will default to [Application.log]
         */
        var logger: Logger? = null

        /**
         * Log messages for calls matching a [predicate]
         */
        fun filter(predicate: (ApplicationCall) -> Boolean) {
            filters.add(predicate)
        }

        /**
         * Put a diagnostic context value to [MDC] with the specified [name] and computed using [provider] function.
         * A value will be available in MDC only during [ApplicationCall] lifetime and will be removed after call
         * processing.
         */
        fun mdc(name: String, provider: (ApplicationCall) -> String?) {
            mdcEntries.add(MDCEntry(name, provider))
        }

        /**
         * Configure application call log message.
         */
        fun format(formatter: (ApplicationCall) -> String) {
            formatCall = formatter
        }
    }

    private val starting: (Application) -> Unit = { log("Application starting: $it") }
    private val started: (Application) -> Unit = { log("Application started: $it") }
    private val stopping: (Application) -> Unit = { log("Application stopping: $it") }
    private var stopped: (Application) -> Unit = {}

    init {
        stopped = {
            log("Application stopped: $it")
            monitor.unsubscribe(ApplicationStarting, starting)
            monitor.unsubscribe(ApplicationStarted, started)
            monitor.unsubscribe(ApplicationStopping, stopping)
            monitor.unsubscribe(ApplicationStopped, stopped)
        }

        monitor.subscribe(ApplicationStarting, starting)
        monitor.subscribe(ApplicationStarted, started)
        monitor.subscribe(ApplicationStopping, stopping)
        monitor.subscribe(ApplicationStopped, stopped)
    }

    internal fun setupMdc(call: ApplicationCall) {
        mdcEntries.forEach { entry ->
            entry.provider(call)?.let { mdcValue ->
                MDC.put(entry.name, mdcValue)
            }
        }
    }

    internal fun cleanupMdc() {
        mdcEntries.forEach {
            MDC.remove(it.name)
        }
    }

    /**
     * Implementation of an [BaseApplicationPlugin] for the [ApplicationCallLogging].
     */
    companion object Plugin : BaseApplicationPlugin<Application, Configuration, ApplicationCallLogging> {
        override val key: AttributeKey<ApplicationCallLogging> = AttributeKey("ApplicationCallLogging")
        override fun install(pipeline: Application, configure: Configuration.() -> Unit): ApplicationCallLogging {
            val loggingPhase = PipelinePhase("ApplicationCallLoggingPhase")
            val configuration = Configuration().apply(configure)
            val feature = ApplicationCallLogging(
                configuration.logger ?: pipeline.log,
                pipeline.environment.monitor,
                configuration.level,
                configuration.filters.toList(),
                configuration.mdcEntries.toList(),
                configuration.formatCall
            )

            pipeline.insertPhaseBefore(ApplicationCallPipeline.Fallback, loggingPhase)

            if (feature.mdcEntries.isNotEmpty()) {
                pipeline.intercept(loggingPhase) {
                    proceed()
                    feature.setupMdc(call)
                    feature.logSuccess(call)
                    feature.cleanupMdc()
                }
            } else {
                pipeline.intercept(loggingPhase) {
                    proceed()
                    feature.logSuccess(call)
                }
            }

            return feature
        }
    }

    private fun log(message: String) = when (level) {
        Level.ERROR -> log.error(message)
        Level.WARN -> log.warn(message)
        Level.INFO -> log.info(message)
        Level.DEBUG -> log.debug(message)
        Level.TRACE -> log.trace(message)
    }

    private fun logSuccess(call: ApplicationCall) {
        if (filters.isEmpty() || filters.any { it(call) }) {
            log(formatCall(call))
        }
    }
}

/**
 * Generates a string representing this [ApplicationRequest] suitable for logging
 */
fun ApplicationRequest.toLogString(): String = "${httpMethod.value} - ${path()}"

private fun defaultFormat(call: ApplicationCall): String = when (val status = call.response.status() ?: "Unhandled") {
    HttpStatusCode.Found -> "$status: ${call.request.toLogString()} -> ${call.response.headers[HttpHeaders.Location]}"
    else -> "$status: ${call.request.toLogString()}"
}

fun ApplicationCallLogging.Configuration.callIdMdc(name: String = "CallId") {
    mdc(name) { it.callId }
}
