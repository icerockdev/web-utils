/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver

import io.ktor.config.ApplicationConfig
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.loadCommonConfiguration
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.KtorExperimentalAPI

/**
 * Netty engine
 *
 * Idea of configuration from
 * @see io.ktor.server.netty.EngineMain
 */
@KtorExperimentalAPI
object NettyEngine {
    private lateinit var engine: NettyApplicationEngine

    fun start(args: Array<String>) {
        val applicationEnvironment = commandLineEnvironment(args)
        engine = NettyApplicationEngine(applicationEnvironment) { loadConfiguration(applicationEnvironment.config) }
        engine.start(wait = false)
    }

    private fun NettyApplicationEngine.Configuration.loadConfiguration(config: ApplicationConfig) {
        val deploymentConfig = config.config("ktor.deployment")
        loadCommonConfiguration(deploymentConfig)
        deploymentConfig.propertyOrNull("requestQueueLimit")?.getString()?.toInt()?.let {
            requestQueueLimit = it
        }
        deploymentConfig.propertyOrNull("shareWorkGroup")?.getString()?.toBoolean()?.let {
            shareWorkGroup = it
        }
        deploymentConfig.propertyOrNull("responseWriteTimeoutSeconds")?.getString()?.toInt()?.let {
            responseWriteTimeoutSeconds = it
        }
    }

    fun stop() {
        stop(3000L, 5000L)
    }

    fun stop(gracePeriod: Long, timeout: Long) {
        engine.stop(gracePeriod, timeout)
    }
}
