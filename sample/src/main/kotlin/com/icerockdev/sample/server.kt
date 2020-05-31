/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.fasterxml.jackson.databind.SerializationFeature
import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import com.icerockdev.exception.ForbiddenException
import com.icerockdev.exception.ServerErrorException
import com.icerockdev.exception.ValidationException
import com.icerockdev.api.request.QueryParser
import com.icerockdev.api.request.receiveQuery
import com.icerockdev.webserver.*
import com.icerockdev.webserver.log.JsonDataLogger
import com.icerockdev.webserver.log.JsonSecret
import com.icerockdev.webserver.log.LoggingConfiguration
import com.icerockdev.webserver.log.jsonLogger
import com.icerockdev.webserver.tools.receiveRequest
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import javax.validation.constraints.Email

@KtorExperimentalAPI
fun Application.main() {
    install(StatusPages) {
        applyStatusConfiguration()
    }

    install(CORS) {
        applyDefaultCORS()
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(DefaultHeaders)
    install(CallLogging) {
        applyDefaultLogging()
        // Log only /api requests
        // filter { call -> call.request.path().startsWith("/api") }
    }
    install(JsonDataLogger) {
        mapperConfiguration = {
            applyObjectMapper()
        }
        loggingConfiguration =
            LoggingConfiguration(
                responseTypes = listOf(AbstractResponse::class, CustomResponse::class),
                requestTypes = listOf(Request::class)
            )
    }
    install(CallId) {
        applyCallConfiguration()
    }
    install(ContentNegotiation) {
        jackson() {
            applyObjectMapper()
            configure(SerializationFeature.INDENT_OUTPUT, false)
        }
    }
    install(QueryParser) {
        mapperConfiguration = {
            applyObjectMapper()
        }
    }

    /**
     * @see <a href="https://ktor.io/advanced/pipeline.html#ktor-pipelines">Ktor Documentation</a>
     */
    intercept(ApplicationCallPipeline.Monitoring, getMonitoringPipeline())

    routing {
        get("/") {
            call.respond("!!!")
        }

        jsonLogger {
            get("/object") {
                call.respond(TestResponse(200, "some message"))
            }

            post("/object") {
                val request = call.receiveRequest<TestRequest>()
                if (!request.isValidRecursive()) {
                    throw ValidationException(request.getErrorList())
                }
                call.respond(TestResponse2(200, request.email, request.password))
            }

            get("/custom-object") {
                call.respond(CustomResponse(200, "Custom message", listOf(1, 2, 3)))
            }
        }

        get("/exception") {
            throw RuntimeException("Some exception")
        }

        get("/handled") {
            throw ServerErrorException("Some handled exception")
        }

        get("/handled2") {
            throw ForbiddenException("Some handled exception")
        }

        get("/get") {
            val params = call.receiveQuery<QueryValues>()
            call.respond(object : AbstractResponse(200, params.toString()) {})
        }
    }
}

class TestRequest(@field:Email val email: String, @JsonSecret val password: String) : Request()
class TestResponse2(status: Int, val email: String, @JsonSecret val password: String) :
    AbstractResponse(status, success = true) {
}

class TestResponse(status: Int, message: String) :
    AbstractResponse(status, message, success = true) {
    @JsonSecret
    val data = message
}

class CustomResponse(var status: Int, var message: String, var list: List<Int>)

data class QueryValues(
    private val email: String = "",
    private val age: Int?,
    private val testValue: Int,
    private val test: List<String>?,
    private val tmp: List<Int> = emptyList()
)
