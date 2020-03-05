/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.util

import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import org.joda.time.DateTime
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.URI
import java.security.MessageDigest

fun String.toURI(): URI = URI.create(this)

fun generatePasswordHash(password: String): String {
    return BCryptPasswordEncoder().encode(password)
}

fun verifyPassword(password: String, passwordHash: String): Boolean {
    return BCryptPasswordEncoder().matches(password, passwordHash)
}

fun generateConfirmationToken(): String {
    val bytes = (DateTime.now().millis.toString() + random(64)).toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

fun generateConfirmationCode(length: Int = 4): String {
    return random(length)
}

private fun random(length: Int): String {
    return (1..length)
        .map { (0..9).random() }
        .joinToString("")
}

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

fun <E> Iterable<E>.replace(old: E, new: E) = map { if (it == old) new else it }

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


fun randomStringGenerator(length: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun getDateTimeString(dateTime: DateTime): String {
    return dateTime.toString("yyyy-MM-dd HH:mm:ss")
}
