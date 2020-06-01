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
    private var errorList: Set<ConstraintViolation<Request>> = emptySet()

    fun validate(): Boolean {
        if (validator == null) {
            throw ValidatorException("Validator doesn't defined")
        }
        errorList = validator.validate(this)
        return errorList.isEmpty()
    }

    fun validateRecursive(propertyPath: String = "*"): Set<ConstraintViolation<Request>> {
        validate()
        val constraintSet = getErrorList().toMutableSet()

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

        errorList = constraintSet
        return errorList
    }

    @JsonIgnore
    fun isValidRecursive(): Boolean {
        return errorList.isEmpty()
    }

    fun getErrorList(): Set<ConstraintViolation<Request>> {
        return errorList
    }

    @JsonIgnore
    fun getMembers() = this::class.memberProperties
}
