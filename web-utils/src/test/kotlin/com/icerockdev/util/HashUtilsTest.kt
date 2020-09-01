package com.icerockdev.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

const val RAW_STRING = "123456"
const val SHA_1_HASH = "7c4a8d09ca3762af61e59520943dc26494f8941b"
const val SHA_256_HASH = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92"
const val SHA_512_HASH =
    "ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413"

class HashUtilsTest {
    @Test
    fun bcryptPasswordHashTest() {
        val passwordHash = HashUtils.generatePasswordHash(RAW_STRING)

        assertTrue {
            HashUtils.verifyPassword(RAW_STRING, passwordHash)
        }
    }

    @Test
    fun hashTest() {
        assertEquals(HashUtils.sha1(RAW_STRING), SHA_1_HASH)
        assertEquals(HashUtils.sha256(RAW_STRING), SHA_256_HASH)
        assertEquals(HashUtils.sha512(RAW_STRING), SHA_512_HASH)
    }
}
