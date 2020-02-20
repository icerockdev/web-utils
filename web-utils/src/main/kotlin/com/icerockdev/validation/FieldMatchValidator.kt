/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.validation

import org.apache.commons.beanutils.BeanUtils
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class FieldMatchValidator : ConstraintValidator<FieldMatch, Any> {
    private var firstFieldName: String? = null
    private var secondFieldName: String? = null

    override fun initialize(constraintAnnotation: FieldMatch) {
        firstFieldName = constraintAnnotation.first
        secondFieldName = constraintAnnotation.second
    }

    override fun isValid(value: Any, context: ConstraintValidatorContext): Boolean {
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