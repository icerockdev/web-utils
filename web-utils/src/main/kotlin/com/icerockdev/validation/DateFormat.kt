/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.validation

import org.joda.time.format.DateTimeFormat
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * Validation annotation to validate date field that 2 fields has match pattern.
 *
 * Example, compare date by format:
 * @DateFormat(pattern = "dd-MM-yyyy")
 */
@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [DateFormatValidator::class])
@MustBeDocumented
annotation class DateFormat(
    val message: String = "{message.key}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val pattern: String
)

class DateFormatValidator : ConstraintValidator<DateFormat, String> {
    private lateinit var pattern: String
    override fun initialize(constraintAnnotation: DateFormat) {
        pattern = constraintAnnotation.pattern
    }

    override fun isValid(value: String?, constraintContext: ConstraintValidatorContext?): Boolean {
        return if (value == null) {
            true
        } else try {
            DateTimeFormat.forPattern(pattern).parseDateTime(value)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

