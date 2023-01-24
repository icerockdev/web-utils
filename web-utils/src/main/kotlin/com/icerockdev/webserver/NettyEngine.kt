/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.loadCommonConfiguration
import io.ktor.server.netty.NettyApplicationEngine
import io.netty.handler.codec.http.HttpObjectDecoder
import io.netty.handler.codec.http.HttpServerCodec

/**
 * Netty engine
 *
 * Idea of configuration from
 * @see io.ktor.server.netty.EngineMain
 */
object NettyEngine {
    private const val DEFAULT_STOP_GRACE_PERIOD = 3000L
    private const val DEFAULT_STOP_TIMEOUT = 5000L
    private lateinit var engine: NettyApplicationEngine

    fun start(args: Array<String>) {
        val applicationEnvironment = commandLineEnvironment(args)
        engine = NettyApplicationEngine(applicationEnvironment) { loadConfiguration(applicationEnvironment.config) }
        engine.start(wait = true)
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
        deploymentConfig.propertyOrNull("requestReadTimeoutSeconds")?.getString()?.toInt()?.let {
            requestReadTimeoutSeconds = it
        }
        deploymentConfig.propertyOrNull("runningLimit")?.getString()?.toInt()?.let {
            runningLimit = it
        }
        deploymentConfig.propertyOrNull("tcpKeepAlive")?.getString()?.toBoolean()?.let {
            tcpKeepAlive = it
        }

        if (deploymentConfig.propertyOrNull("httpServer") != null) {
            val httpServerConfig = deploymentConfig.config("httpServer")

            val maxInitialLineLength = httpServerConfig.propertyOrNull("maxInitialLineLength")?.getString()?.toInt()
                ?: HttpObjectDecoder.DEFAULT_MAX_INITIAL_LINE_LENGTH
            val maxHeaderSize = httpServerConfig.propertyOrNull("maxHeaderSize")?.getString()?.toInt()
                ?: HttpObjectDecoder.DEFAULT_MAX_HEADER_SIZE
            val maxChunkSize = httpServerConfig.propertyOrNull("maxChunkSize")?.getString()?.toInt()
                ?: HttpObjectDecoder.DEFAULT_MAX_CHUNK_SIZE

            httpServerCodec = {
                HttpServerCodec(maxInitialLineLength, maxHeaderSize, maxChunkSize)
            }
        }
    }

    fun stop() {
        stop(DEFAULT_STOP_GRACE_PERIOD, DEFAULT_STOP_TIMEOUT)
    }

    fun stop(gracePeriod: Long, timeout: Long) {
        engine.stop(gracePeriod, timeout)
    }
}
