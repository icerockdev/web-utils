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
 * Example, validate field value contains in enum:
 * @field:InIntListByEnum(IAvailableIntByEnum = Status::class, message = "Should it be 30 or 40")
 */
interface IAvailableIntByEnum {
    fun getAvailableIntList(listName: String): List<Int>
}

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [InIntListByEnumValidator::class])
@kotlin.annotation.Retention
@ReportAsSingleViolation
annotation class InIntListByEnum(
    val message: String = "Not exists in integer array",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = [],
    val nullable: Boolean = true,
    val listName: String = "all",
    val IAvailableIntByEnum: KClass<out IAvailableIntByEnum>
)

class InIntListByEnumValidator : ConstraintValidator<InIntListByEnum, Int> {

    private var annotationValue: List<Int> = emptyList()
    private var annotationNullable: Boolean = true

    override fun initialize(constraintAnnotation: InIntListByEnum) {
        this.annotationNullable = constraintAnnotation.nullable
        val clazz = constraintAnnotation.IAvailableIntByEnum.java
        if (!clazz.isEnum) {
            throw IllegalAccessException("Only enum class supported for validation")
        }

        annotationValue = clazz.enumConstants.first().getAvailableIntList(constraintAnnotation.listName)
    }

    override fun isValid(fieldValue: Int?, constraintContext: ConstraintValidatorContext?): Boolean {
        if (fieldValue == null) {
            return this.annotationNullable
        }
        return fieldValue in this.annotationValue
    }
}
