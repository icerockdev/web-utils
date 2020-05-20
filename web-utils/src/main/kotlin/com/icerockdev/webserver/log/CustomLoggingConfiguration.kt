package com.icerockdev.webserver.log

import kotlin.reflect.KClass

class CustomLoggingConfiguration(
    val requestTypes: List<KClass<*>> = listOf(),
    val responseTypes: List<KClass<*>> = listOf()
)
