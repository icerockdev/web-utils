/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.validation

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.ReportAsSingleViolation
import kotlin.reflect.KClass

/**
 * Validation annotation to validate field than contains in array.
 *
 * Example, validate field value contains in array:
 * @field:InIntArray(value = [10, 20], message = "Should it be 10 or 20")
 */
@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [InIntArrayValidator::class])
@kotlin.annotation.Retention
@ReportAsSingleViolation
annotation class InIntArray(
    val message: String = "Not exists in integer array",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = [],
    val value: IntArray, // Annotation value allowed types https://stackoverflow.com/questions/1458535/which-types-can-be-used-for-java-annotation-members
    val nullable: Boolean = true
)

class InIntArrayValidator : ConstraintValidator<InIntArray, Int> {

    private var annotationValue: IntArray = intArrayOf()
    private var annotationNullable: Boolean = true

    override fun initialize(constraintAnnotation: InIntArray) {
        this.annotationValue = constraintAnnotation.value
        this.annotationNullable = constraintAnnotation.nullable
    }

    override fun isValid(fieldValue: Int?, constraintContext: ConstraintValidatorContext?): Boolean {
        if (fieldValue == null) {
            return this.annotationNullable
        }
        return fieldValue in this.annotationValue
    }
}
