/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.i18n

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import java.util.Locale
import javax.validation.MessageInterpolator

class I18NMessageInterpolator(private val i18n: I18N) : ParameterMessageInterpolator() {
    override fun interpolate(messageTemplate: String?, context: MessageInterpolator.Context?): String {
        return interpolateMessage(messageTemplate, context, i18n.locale)
    }

    override fun interpolate(messageTemplate: String?, context: MessageInterpolator.Context?, locale: Locale?): String {
        return interpolateMessage(messageTemplate, context, locale)
    }

    private fun interpolateMessage(message: String? , context: MessageInterpolator.Context?, locale: Locale?): String {
       return i18n.t(super.interpolate(message, context, locale), null, locale)
    }
}
