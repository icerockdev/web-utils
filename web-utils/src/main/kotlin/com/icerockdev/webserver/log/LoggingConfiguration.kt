package com.icerockdev.webserver.log

import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import kotlin.reflect.KClass

class LoggingConfiguration(
    val requestTypes: List<KClass<*>> = listOf(Request::class),
    val responseTypes: List<KClass<*>> = listOf(AbstractResponse::class)
)
