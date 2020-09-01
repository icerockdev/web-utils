package com.icerockdev.util

import at.favre.lib.crypto.bcrypt.BCrypt
import java.security.MessageDigest

// To avoid using springframework components and keep backward compatibility
// GENSALT_DEFAULT_LOG2_ROUNDS in org.springframework.security.crypto.bcrypt.Bcrypt
const val BCRYPT_COST = 10

object HashUtils {
    fun sha512(input: String): String = hash("SHA-512", input)

    fun sha256(input: String): String = hash("SHA-256", input)

    fun sha1(input: String): String = hash("SHA-1", input)

    fun generatePasswordHash(password: String): String {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }

    fun verifyPassword(password: String, passwordHash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified
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


    private fun hash(algorithm: String, input: String): String {
        return MessageDigest
            .getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }
}
