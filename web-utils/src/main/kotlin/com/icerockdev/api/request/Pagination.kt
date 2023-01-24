/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api.request

import io.ktor.server.application.ApplicationCall

class Pagination(
    val limit: Int = 10,
    val offset: Int = 0,
    val sortBy: String = "id",
    val orderBy: String = "DESC"
)

fun ApplicationCall.getRequestPagination(
    limit: Int = 10,
    offset: Int = 0,
    sortBy: String = "id",
    orderBy: String = "DESC"
): Pagination {
    return Pagination(
        limit = this.parameters["limit"]?.toInt() ?: limit,
        offset = this.parameters["offset"]?.toInt() ?: offset,
        sortBy = this.parameters["sortBy"] ?: sortBy,
        orderBy = this.parameters["orderBy"] ?: orderBy
    )
}
