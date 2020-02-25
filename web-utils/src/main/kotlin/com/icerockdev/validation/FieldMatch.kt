/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.validation

import org.apache.commons.beanutils.BeanUtils
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * Validation annotation to validate that 2 fields have the same value.
 * An array of fields and their matching confirmation fields can be supplied.
 *
 * Example, compare 1 pair of fields:
 * @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match")
 *
 * Example, compare more than 1 pair of fields:
 * @FieldMatch.List([
 *      @FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match"),
 *      @FieldMatch(first = "email", second = "confirmEmail", message = "The email fields must match")})
 * ])
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.ANNOTATION_CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [FieldMatchValidator::class])
@MustBeDocumented
annotation class FieldMatch(
    val message: String = "{constraints.fieldmatch}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    /**
     * @return The first field
     */
    val first: String,
    /**
     * @return The second field
     */
    val second: String
) {
    /**
     * Defines several `@FieldMatch` annotations on the same element
     *
     * @see FieldMatch
     */
    @Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.ANNOTATION_CLASS)
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    annotation class List(val value: Array<FieldMatch>)
}

class FieldMatchValidator : ConstraintValidator<FieldMatch, Any> {
    private var firstFieldName: String? = null
    private var secondFieldName: String? = null

    override fun initialize(constraintAnnotation: FieldMatch) {
        firstFieldName = constraintAnnotation.first
        secondFieldName = constraintAnnotation.second
    }

    override fun isValid(value: Any, constraintContext: ConstraintValidatorContext?): Boolean {
        try {
            val firstObj = BeanUtils.getProperty(value, firstFieldName)
            val secondObj = BeanUtils.getProperty(value, secondFieldName)

            return firstObj == null && secondObj == null || firstObj != null && firstObj == secondObj
        } catch (ignore: Exception) {
            // ignore
        }

        return true
    }
}
