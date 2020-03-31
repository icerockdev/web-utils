package com.icerockdev.validation

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationTarget.ANNOTATION_CLASS
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.TYPE_PARAMETER
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext


/**
 * Annotation to validate each list element (is not empty).
 *
 * Example, validate field
 * @field:NoNullElements
*/
@MustBeDocumented
@Constraint(validatedBy = [NoNullElementsValidator::class])
@Target(allowedTargets = [FUNCTION, FIELD, ANNOTATION_CLASS, CONSTRUCTOR, VALUE_PARAMETER, TYPE_PARAMETER])
@Retention(AnnotationRetention.RUNTIME)
annotation class NoNullElements(
    val message: String = "must not contain null elements",
    val groups: Array<KClass<out Any>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NoNullElementsValidator : ConstraintValidator<NoNullElements, Collection<Any>> {
    override fun isValid(value: Collection<Any>?, context: ConstraintValidatorContext): Boolean {
        // null values are valid
        if (value == null) {
            return true
        }
        return value.stream().noneMatch {
            it == null
        }
    }
}