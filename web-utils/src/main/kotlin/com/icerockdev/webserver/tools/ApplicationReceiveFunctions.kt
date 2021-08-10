/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver.tools

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.icerockdev.exception.BadRequestException
import io.ktor.application.ApplicationCall
import io.ktor.request.receive
import kotlin.reflect.KClass

suspend inline fun <reified T : Any> ApplicationCall.receiveRequest(): T {
    return try {
        receive()
    } catch (e: MissingKotlinParameterException) {
        throw BadRequestException(e)
    } catch (e: Throwable) {
        application.environment.log.error(e.localizedMessage, e)
        throw BadRequestException(message = "Invalid request body")
    }
}

suspend inline fun <reified T : Any> ApplicationCall.receiveRequest(type: KClass<T>): T {
    return try {
        receive(type)
    } catch (e: MissingKotlinParameterException) {
        throw BadRequestException(e)
    } catch (e: Throwable) {
        application.environment.log.error(e.localizedMessage, e)
        throw BadRequestException(message = "Invalid request body")
    }
}
