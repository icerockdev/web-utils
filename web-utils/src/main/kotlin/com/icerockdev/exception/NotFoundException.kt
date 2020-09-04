/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

import io.ktor.http.HttpStatusCode

class NotFoundException(message: String = HttpStatusCode.NotFound.description) :
    UserException(HttpStatusCode.NotFound.value, message)
