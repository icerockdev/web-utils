/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import com.icerockdev.exception.ForbiddenException
import com.icerockdev.exception.ServerErrorException
import com.icerockdev.webserver.log.JsonDataLogger
import com.icerockdev.webserver.*
import com.icerockdev.webserver.log.JsonSecret
import com.icerockdev.webserver.tools.receiveRequest
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.request.queryString
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI

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

    /**
     * @see <a href="https://ktor.io/advanced/pipeline.html#ktor-pipelines">Ktor Documentation</a>
     */
    intercept(ApplicationCallPipeline.Monitoring, getMonitoringPipeline())

    routing {
        get("/") {
            println(call.request.queryString())
            call.respond("!!!")
        }

        get("/object") {
            call.respond(TestResponse(200, "some message"))
        }

        post("/object") {
            val request = call.receiveRequest<TestRequest>()
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
    }
}

class TestRequest(val email: String, @JsonSecret val password: String): Request()
class TestResponse2(status: Int, val email: String, @JsonSecret val password: String) :
    AbstractResponse(status, isSuccess = true) {
}

class TestResponse(status: Int, message: String) :
    AbstractResponse(status, message, isSuccess = true) {
    @JsonSecret
    val data = message
}