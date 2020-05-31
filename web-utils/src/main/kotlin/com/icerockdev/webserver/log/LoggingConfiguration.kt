package com.icerockdev.webserver.log

import kotlin.reflect.KClass

class LoggingConfiguration(
    val requestTypes: List<KClass<*>>,
    val responseTypes: List<KClass<*>>
)
