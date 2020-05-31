/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.icerockdev.exception.ValidatorException
import javax.validation.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/*
* Annotations docs for Kotlin - https://kotlinlang.org/docs/reference/annotations.html?_ga=2.133813446.951009214.1566383426-111422153.1543224819#annotation-use-site-targets
* Example for Javax validation annotations - https://www.baeldung.com/javax-validation
* Live example - service/tools/src/test/kotlin/ToolsTest.kt
 */
abstract class Request(
    private val messageInterpolator: MessageInterpolator? = null,
    validatorFactory: ValidatorFactory = Validation.byDefaultProvider()
        .configure()
        .messageInterpolator(messageInterpolator)
        .buildValidatorFactory()
) {
    private val validator: Validator? = validatorFactory.validator
    private var errorList: Set<ConstraintViolation<Request>> = setOf()

    // TODO: Make as private
    @Deprecated("Need use isValidRecursive() and getErrorList()", ReplaceWith("getErrorList()"))
    fun validate(): Set<ConstraintViolation<Request>> {
        if (validator == null) {
            throw ValidatorException("Validator doesn't defined")
        }

        return validator.validate(this)
    }

    fun validateRecursive(propertyPath: String = "*"): Set<ConstraintViolation<Request>> {
        val constraintSet = validate().toMutableSet()

        @Suppress("UNCHECKED_CAST")
        val self = this::class as KClass<Request>
        val kProperties = self.memberProperties

        for (kProperty in kProperties) {
            val property = kProperty.get(this)
            val propertyName = kProperty.name
            when (property) {
                is Request -> {
                    constraintSet.addAll(property.validateRecursive("$propertyPath -> $propertyName"))
                }
                is List<*> -> {
                    property.forEach { listItem ->
                        if (listItem is Request) {
                            constraintSet.addAll(listItem.validateRecursive("$propertyPath -> $propertyName"))
                        }
                    }
                }
            }
        }

        return constraintSet
    }

    @JsonIgnore
    @Deprecated("Need use isValidRecursive() and getErrorList()", ReplaceWith("isValidRecursive()"))
    fun isValid(): Boolean {
        return validate().isEmpty()
    }

    @JsonIgnore
    fun isValidRecursive(): Boolean {
        errorList = validateRecursive()
        return errorList.isEmpty()
    }

    fun getErrorList(): Set<ConstraintViolation<Request>> {
        return errorList
    }

    @JsonIgnore
    fun getMembers() = this::class.memberProperties
}
