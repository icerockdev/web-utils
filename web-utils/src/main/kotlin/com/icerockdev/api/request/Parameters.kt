package com.icerockdev.api.request

import io.ktor.http.Parameters

fun Parameters.parseFilters(): HashMap<String, String> {
    val parameters = this
    val filters: HashMap<String, String> = hashMapOf()
    parameters.entries().forEach {
        if (it.key.contains("filters.")) {
            val key = it.key.substringAfter(".").toUpperCase()
            val value = parameters[it.key]

            if (value != null) {
                filters[key] = value
            }
        }
    }

    return filters
}
