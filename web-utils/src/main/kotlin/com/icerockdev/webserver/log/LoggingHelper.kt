package com.icerockdev.webserver.log

internal object LoggingHelper {
    fun entriesToJsonString(entries: Set<Map.Entry<String, List<String>>>): String? {
        if (entries.isEmpty()) {
            return null
        }

        val content = entries.joinToString(separator = ",\n") { entry ->
            entry.value.joinToString(separator = ",\n") {
                String.format("\"%s\": \"%s\"", entry.key, it.replace("\"", "\\\""))
            }
        }

        return String.format("{\n%s\n}", content)
    }

    fun replaceSecretFieldsValueInJsonString(
        originalString: String,
        secretFieldList: List<String>,
        replaceMask: String = "*****"
    ): String {
        var replacedString: String = originalString
        secretFieldList.forEach { field ->
            replacedString = replacedString.replace(
                Regex("\"$field\"\\s*:\\s*(\"[^\"]*\"|[^\\s,{}\"]*)", RegexOption.IGNORE_CASE),
                "\"$field\":\"$replaceMask\""
            )
        }

        return replacedString
    }
}
