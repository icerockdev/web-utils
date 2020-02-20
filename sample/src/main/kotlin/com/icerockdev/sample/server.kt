/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.webserver.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.queryString
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing

fun Application.main() {
    install(StatusPages, getStatusConfiguration())
    install(CORS) {
        applyDefaultCORS()
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(DefaultHeaders)
    install(CallLogging) {
        applyDefaultLogging()
        applyApiFilter()
    }
    install(CallId, getCallConfiguration())
    install(ContentNegotiation, defaultContentNegotiation())

    /**
     * @see <a href="https://ktor.io/advanced/pipeline.html#ktor-pipelines">Ktor Documentation</a>
     */
    intercept(ApplicationCallPipeline.Monitoring, getMonitoringPipeline())

    routing {
        get("/") {
            println(call.request.queryString())
            call.respond("!!!")
        }
    }
}