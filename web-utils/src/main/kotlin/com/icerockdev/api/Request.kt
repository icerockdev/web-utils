/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.icerockdev.exception.ValidatorException
import javax.validation.ConstraintViolation
import javax.validation.MessageInterpolator
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
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

    @JsonIgnore
    fun validate(): Boolean {
        if (validator == null) {
            throw ValidatorException("Validator doesn't defined")
        }
        errorList = validator.validate(this)
        return errorList.isEmpty()
    }

    @JsonIgnore
    fun validateRecursive(propertyPath: String = "*"): Boolean {
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
                    val isValid = property.validateRecursive("$propertyPath -> $propertyName")
                    if (!isValid) {
                        constraintSet.addAll(property.getErrorList())
                    }
                }
                is List<*> -> {
                    property.forEach { listItem ->
                        if (listItem is Request) {
                            val isValid = listItem.validateRecursive("$propertyPath -> $propertyName")
                            if (!isValid) {
                                constraintSet.addAll(listItem.getErrorList())
                            }
                        }
                    }
                }
            }
        }

        errorList = constraintSet
        return errorList.isEmpty()
    }

    @JsonIgnore
    fun getErrorList(): Set<ConstraintViolation<Request>> {
        return errorList
    }

    @JsonIgnore
    fun getMembers() = this::class.memberProperties
}
