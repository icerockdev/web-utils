package com.icerockdev.util

import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.Socket

object CheckNetwork {
    private val logger = LoggerFactory.getLogger(CheckNetwork::class.java)

    fun isTcpPortAvailable(host: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.reuseAddress = true;
                socket.connect(InetSocketAddress(host, port), timeoutMs)
            }
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun awaitService(host: String, port: Int, checkIntervalMs: Long = 1000L) {
        logger.info("Await connection. Host: $host, Port: $port")
        while (!isTcpPortAvailable(host, port, 1000)) {
            Thread.sleep(checkIntervalMs)
        }
        logger.info("Connection ready. Host: $host, Port: $port")
    }
}

