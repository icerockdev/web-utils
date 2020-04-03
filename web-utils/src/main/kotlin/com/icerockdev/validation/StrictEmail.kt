/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.validation

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Email
@Pattern(regexp = ".+@.+\\..+", message = "{javax.validation.constraints.Email.message}")
@Constraint(validatedBy = [])
@MustBeDocumented
annotation class StrictEmail(
    val message: String = "{javax.validation.constraints.Email.message}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
