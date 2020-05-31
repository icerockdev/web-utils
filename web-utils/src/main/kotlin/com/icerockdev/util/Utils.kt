/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.util

import org.joda.time.DateTime
import java.net.URI

fun String.toURI(): URI = URI.create(this)

fun <E> Iterable<E>.replace(old: E, new: E) = map { if (it == old) new else it }

fun getDateTimeString(dateTime: DateTime): String {
    return dateTime.toString("yyyy-MM-dd HH:mm:ss")
}
