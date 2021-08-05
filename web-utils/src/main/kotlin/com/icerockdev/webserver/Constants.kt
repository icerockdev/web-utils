/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.webserver


object Constants {
    const val LOG_FIELD_HEADERS: String = "headers"
    const val LOG_FIELD_QUERY_PARAMETERS: String = "queryParameters"
    const val LOG_FIELD_HTTP_METHOD: String = "httpMethod"
    const val LOG_FIELD_REQUEST_PATH: String = "requestPath"
    const val LOG_FIELD_REQUEST_BODY: String = "requestBody"
    const val LOG_FIELD_RESPONSE_BODY: String = "responseBody"
    const val LOG_FIELD_TRACE_UUID: String = "traceUUID"
    const val LOG_FIELD_STATUS_CODE: String = "statusCode"
    const val LOG_FIELD_ENV: String = "env"
    const val LOG_FIELD_USER_ID: String = "userId"
}

enum class Environment(val value: String) {
    DEV("dev"),
    PROD("prod"),
    LOCAL("local")
}
