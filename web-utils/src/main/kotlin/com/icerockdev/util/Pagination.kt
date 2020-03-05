/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.util

class Pagination(
    val limit: Int = 10,
    val offset: Int = 0,
    val sortBy: String = "id",
    val orderBy: String = "DESC"
)
