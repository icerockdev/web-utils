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
 * @field:InStringListByEnum(IAvailableStringByEnum = Status::class, message = "Invalid value")
 */
interface IAvailableStringByEnum {
    fun getAvailableStringList(listName: String): List<String>
}

@Target(AnnotationTarget.FIELD)
@MustBeDocumented
@Constraint(validatedBy = [InStringListByEnumValidator::class])
@kotlin.annotation.Retention
@ReportAsSingleViolation
annotation class InStringListByEnum(
    val message: String = "Not exists in string array",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Any>> = [],
    val nullable: Boolean = true,
    val listName: String = "all",
    val IAvailableStringByEnum: KClass<out IAvailableStringByEnum>
)

class InStringListByEnumValidator : ConstraintValidator<InStringListByEnum, String> {

    private var annotationValue: List<String> = emptyList()
    private var annotationNullable: Boolean = true

    override fun initialize(constraintAnnotation: InStringListByEnum) {
        this.annotationNullable = constraintAnnotation.nullable
        val clazz = constraintAnnotation.IAvailableStringByEnum.java
        if (!clazz.isEnum) {
            throw IllegalAccessException("Only enum class supported for validation")
        }

        annotationValue = clazz.enumConstants.first().getAvailableStringList(constraintAnnotation.listName)
    }

    override fun isValid(fieldValue: String?, constraintContext: ConstraintValidatorContext?): Boolean {
        if (fieldValue == null) {
            return this.annotationNullable
        }
        return fieldValue in this.annotationValue
    }
}
