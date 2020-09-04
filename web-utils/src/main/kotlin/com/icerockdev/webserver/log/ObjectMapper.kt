package com.icerockdev.webserver.log

import com.fasterxml.jackson.annotation.JacksonAnnotation
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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


fun ObjectMapper.applyDefaultConfiguration() {
    configure(SerializationFeature.INDENT_OUTPUT, true)
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    dateFormat = StdDateFormat()
    registerKotlinModule()
}

fun ObjectMapper.applyPrettyPrintConfiguration() {
    setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
        indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
        indentObjectsWith(DefaultIndenter("  ", "\n"))
    })
}

fun ObjectMapper.applyJsonSecretConfiguration() {
    // intercept custom annotation and secret replace value
    setAnnotationIntrospector(object : JacksonAnnotationIntrospector() {
        override fun findSerializer(annotated: Annotated?): Any? {
            val ann = annotated?.getAnnotation(JsonSecret::class.java)
            if (ann != null) {
                return SecretSerializer::class.java
            }
            return super.findSerializer(annotated)
        }
    })
}
