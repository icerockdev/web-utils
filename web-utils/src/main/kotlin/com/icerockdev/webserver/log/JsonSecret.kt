package com.icerockdev.webserver.log

import com.fasterxml.jackson.annotation.JacksonAnnotation
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import java.io.IOException

@JacksonAnnotation
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonSecret

@JacksonStdImpl
class SecretSerializer : StdScalarSerializer<Any?>(String::class.java, false) {

    @Throws(IOException::class)
    override fun serializeWithType(
        value: Any?, gen: JsonGenerator, provider: SerializerProvider,
        typeSer: TypeSerializer
    ) { // no type info, just regular serialization
        gen.writeString("****")
    }

    override fun serialize(value: Any?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen!!.writeString("****")
    }
}
