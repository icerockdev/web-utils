package com.icerockdev.webserver

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.icerockdev.webserver.log.JsonSecret
import com.icerockdev.webserver.log.SecretSerializer

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
