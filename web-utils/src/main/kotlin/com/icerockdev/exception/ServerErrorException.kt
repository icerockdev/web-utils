/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.exception

import io.ktor.http.HttpStatusCode


class ServerErrorException(message: String = HttpStatusCode.InternalServerError.description) :
    UserException(HttpStatusCode.InternalServerError.value, message)
