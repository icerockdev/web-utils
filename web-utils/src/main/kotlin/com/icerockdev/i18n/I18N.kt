/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.i18n

import gnu.gettext.GettextResource
import java.util.*


class I18N(
    private val locale: Locale = Locale.getDefault(),
    private val defaultCategory: String
) {

    /**
     * http://khpi-iip.mipk.kharkiv.edu/library/extent/prog/inter/concept.html
     *
     * Если класс ResourceBundle для заданной Locale не существует, getBundle пытается найти наиболее соответствующий.
     * Например, если требуемый класс - ButtonLabel_fr_CA_UNIX,
     * а Locale по умолчанию - en_US, getBundle будет искать классы в следующем порядке:
     * ButtonLabel_fr_CA_UNIX
     * ButtonLabel_fr_CA
     * ButtonLabel_fr
     * ButtonLabel_en_US
     * ButtonLabel_en
     * ButtonLabel
     */
    private fun gettext(text: String, category: String? = null, currentLocale: Locale? = null): String {
        return try {
            val resource = ResourceBundle.getBundle(category ?: defaultCategory, currentLocale ?: locale)
            GettextResource.gettext(resource, text)
        } catch (exception: MissingResourceException) {
            text
        }
    }

    fun t(text: String, category: String? = null, currentLocale: Locale? = null): String {
        return gettext(text, category, currentLocale)
    }

    fun nt(msgId: String, msgIdPlural: String, n: Long) {
        // TODO("Need fix plural function. GNU ngettext does not work correctly")
//        return MessageFormat.format(GettextResource.ngettext(resource, msgId, msgIdPlural, n), n)
    }

}
