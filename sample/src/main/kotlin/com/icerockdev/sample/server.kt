/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import com.icerockdev.exception.ForbiddenException
import com.icerockdev.exception.ServerErrorException
import com.icerockdev.exception.ValidationException
import com.icerockdev.util.QueryParser
import com.icerockdev.util.receiveQuery
import com.icerockdev.validation.InIntArray
import com.icerockdev.validation.NoNullElements
import com.icerockdev.validation.UseAnnotations
import com.icerockdev.webserver.*
import com.icerockdev.webserver.log.JsonDataLogger
import com.icerockdev.webserver.log.JsonSecret
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
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@KtorExperimentalAPI
fun Application.main() {
    install(StatusPages, getStatusConfiguration())
    install(CORS) {
        applyDefaultCORS()
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(DefaultHeaders)
    install(CallLogging) {
        applyDefaultLogging()
    }
    install(JsonDataLogger) {
        mapperConfiguration = getObjectMapper()
    }
    install(CallId, getCallConfiguration())
    install(ContentNegotiation) {
        jackson(block = getObjectMapper())
    }
    install(QueryParser) {
        mapperConfiguration = getObjectMapper()
    }

    /**
     * @see <a href="https://ktor.io/advanced/pipeline.html#ktor-pipelines">Ktor Documentation</a>
     */
    intercept(ApplicationCallPipeline.Monitoring, getMonitoringPipeline())

    routing {
        get("/") {
            call.respond("!!!")
        }

        get("/object") {
            call.respond(TestResponse(200, "some message"))
        }

        post("/object") {
            val request = call.receiveRequest<TestRequest>()
            if (!request.isValid()) {
                throw ValidationException(request.validate())
            }
            call.respond(TestResponse2(200, request.email, request.password))
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

class TestRequest(val email: String, @JsonSecret val password: String) : Request()
class TestResponse2(status: Int, val email: String, @JsonSecret val password: String) :
    AbstractResponse(status, isSuccess = true) {
}

class TestResponse(status: Int, message: String) :
    AbstractResponse(status, message, isSuccess = true) {
    @JsonSecret
    val data = message
}

data class QueryValues(
    private val email: String = "",
    private val age: Int?,
    private val testValue: Int,
    private val test: List<String>?,
    private val tmp: List<Int> = emptyList()
)