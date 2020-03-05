/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.api

import com.fasterxml.jackson.annotation.JsonIgnore
import com.icerockdev.exception.ValidatorException
import javax.validation.*
import kotlin.reflect.full.memberProperties


/*
* Annotations docs for Kotlin - https://kotlinlang.org/docs/reference/annotations.html?_ga=2.133813446.951009214.1566383426-111422153.1543224819#annotation-use-site-targets
* Example for Javax validation annotations - https://www.baeldung.com/javax-validation
* Live example - service/tools/src/test/kotlin/ToolsTest.kt
 */
abstract class Request(
    private val messageInterpolator: MessageInterpolator? = null,
    validatorBuilder: ValidatorFactory = Validation.byDefaultProvider().configure()
        .messageInterpolator(messageInterpolator)
        .buildValidatorFactory()
) {

    private val validator: Validator? = validatorBuilder.validator

    fun validate(): Set<ConstraintViolation<Request>> {
        if (validator == null) {
            throw ValidatorException("Validator doesn't defined")
        }

        return validator.validate(this)
    }

    @JsonIgnore
    fun isValid(): Boolean {
        return validate().isEmpty()
    }

    @JsonIgnore
    fun getMembers() = this::class.memberProperties
}