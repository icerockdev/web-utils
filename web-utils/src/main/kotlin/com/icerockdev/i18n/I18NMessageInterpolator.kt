/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.i18n

import java.util.*
import javax.validation.MessageInterpolator

class I18NMessageInterpolator(private val i18n: I18N) : MessageInterpolator {
    override fun interpolate(messageTemplate: String?, context: MessageInterpolator.Context?): String {
        return i18n.t(messageTemplate ?: "")
    }

    override fun interpolate(messageTemplate: String?, context: MessageInterpolator.Context?, locale: Locale?): String {
        return i18n.t(messageTemplate ?: "", "", locale)
    }
}