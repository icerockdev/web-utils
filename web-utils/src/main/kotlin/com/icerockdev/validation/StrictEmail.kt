/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.validation

import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator
import org.hibernate.validator.internal.util.logging.LoggerFactory
import java.lang.invoke.MethodHandles
import java.util.regex.Pattern.compile
import java.util.regex.PatternSyntaxException
import javax.validation.Constraint
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import javax.validation.constraints.Pattern
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.TYPE_PARAMETER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

const val DEFAULT_EMAIL_REGEXP = ".+@.+\\..+"

/**
 * Annotation to validate email
 *
 * Example, validate field
 * @field:StrictEmail(message = "Invalid email")
 */
@Constraint(validatedBy = [StrictEmailValidator::class])
@Target(allowedTargets = [FUNCTION, FIELD, ANNOTATION_CLASS, CONSTRUCTOR, VALUE_PARAMETER, TYPE_PARAMETER])
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class StrictEmail(
    val message: String = "{javax.validation.constraints.Email.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    val regexp: String = DEFAULT_EMAIL_REGEXP,
    val flags: Array<Pattern.Flag> = []
)

class StrictEmailValidator : AbstractEmailValidator<StrictEmail?>() {
    private var pattern: java.util.regex.Pattern = compile(DEFAULT_EMAIL_REGEXP, 0)
    override fun initialize(constraintAnnotation: StrictEmail?) {
        super.initialize(constraintAnnotation)
        val flags: Array<Pattern.Flag> = constraintAnnotation?.flags ?: arrayOf()
        var intFlag = 0
        for (flag in flags) {
            intFlag = intFlag.or(flag.value)
        }

        if (DEFAULT_EMAIL_REGEXP != constraintAnnotation?.regexp || constraintAnnotation.flags.isNotEmpty()) {
            pattern = try {
                compile(constraintAnnotation?.regexp ?: DEFAULT_EMAIL_REGEXP, intFlag)
            } catch (e: PatternSyntaxException) {
                throw logger.getInvalidRegularExpressionException(e)
            }
        }
    }

    override fun isValid(value: CharSequence, context: ConstraintValidatorContext): Boolean {
        val isValid = super.isValid(value, context)
        if (!isValid) {
            return isValid
        }
        val m = pattern.matcher(value)
        return m.matches()
    }

    companion object {
        private val logger = LoggerFactory.make(MethodHandles.lookup())
    }
}
