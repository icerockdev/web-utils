package com.icerockdev.webserver.log

import kotlin.reflect.KClass

class LoggingConfiguration(
    val responseTypes: List<KClass<*>>
)
