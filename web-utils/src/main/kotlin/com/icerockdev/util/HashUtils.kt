package com.icerockdev.util

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.security.MessageDigest

object HashUtils {
    fun sha512(input: String) = hashString("SHA-512", input)

    fun sha256(input: String) = hashString("SHA-256", input)

    fun sha1(input: String) = hashString("SHA-1", input)

    fun generatePasswordHash(password: String): String {
        return BCryptPasswordEncoder().encode(password)
    }

    fun verifyPassword(password: String, passwordHash: String): Boolean {
        return BCryptPasswordEncoder().matches(password, passwordHash)
    }

    fun generateRandomToken(): String {
        return sha256(System.currentTimeMillis().toString() + generateRandomString(64))
    }

    fun generateRandomCode(length: Int): String {
        return (1..length)
            .map { (0..9).random() }
            .joinToString("")
    }

    fun generateRandomString(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charPool.random() }
            .joinToString("")
    }


    private fun hashString(type: String, input: String): String {
        return MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }
}
