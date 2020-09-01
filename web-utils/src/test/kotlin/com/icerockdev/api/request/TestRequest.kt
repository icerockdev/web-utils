package com.icerockdev.api.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.icerockdev.api.Request
import com.icerockdev.i18n.I18N
import com.icerockdev.i18n.I18NMessageInterpolator
import com.icerockdev.validation.*
import java.util.*
import javax.validation.constraints.*

enum class Status(val value: Int) : IAvailableIntByEnum {
    STATUS_ACTIVE(30),
    STATUS_BLOCKED(40);

    override fun getAvailableIntList(listName: String): List<Int> {
        return when (listName) {
            "all" -> arrayListOf(STATUS_ACTIVE.value, STATUS_BLOCKED.value)
            else -> {
                emptyList()
            }
        }
    }
}

enum class Mode : IAvailableStringByEnum {
    ACTIVE,
    PASSIVE;

    override fun getAvailableStringList(listName: String): List<String> {
        return when (listName) {
            "all" -> arrayListOf(ACTIVE.name, PASSIVE.name)
            else -> emptyList()
        }
    }
}

val interpolator = I18NMessageInterpolator(I18N(locale = Locale("ru", "RU"), defaultCategory = "i18n.compile.messages"))

@FieldMatch(message = "The password fields must match", first = "password", second = "passwordRepeat")
class TestRequest(
    @field:NotNull(message = "Name is required")
    var name: String? = "Mike",
    @field:NotNull(message = "Title is required")
    var title: String? = "Mrs",
    @field:Size(min = 2, max = 14, message = "Invalid size of text")
    var text: String = "Test",
    @field:Max(14)
    @field:Min(2)
    @field:InIntArray(value = [10, 20], message = "Should it be 10 or 20")
    var age: Int = 10,
    @field:InIntListByEnum(IAvailableIntByEnum = Status::class, message = "Should it be 30 or 40")
    var status: Int = 30,
    @field:InStringListByEnum(IAvailableStringByEnum = Mode::class, message = "Should be ACTIVE or PASSIVE")
    var mode: String = "ACTIVE",
    @field:StrictEmail(message = "Invalid email")
    var email: String = "test@test.er",
    @field:DateFormat(message = "Date must be in YYYY-MM-DD format", pattern = "YYYY-MM-DD")
    @field:NotNull(message = "Date is required")
    var date: String = "2020-02-25",
    @field:NotNull(message = "Password is required")
    var password: String = "123456",
    @field:NotNull(message = "Password repeat is required")
    var passwordRepeat: String = "123456",
    var nested: NestedTestRequest = NestedTestRequest(
        macAddress = "30:AE:A4:89:40:E0",
        list = mutableListOf(
            NestedTestListItemRequest(1, "test1"),
            NestedTestListItemRequest(2, "test2"),
            NestedTestListItemRequest(3, "test3")
        )
    )
) : Request(interpolator)

class NestedTestRequest(
    @field:NotNull(message = "MAC is required field")
    @field:Size(message = "Invalid length of MAC", min = 17, max = 17)
    @field:Pattern(
        message = "Invalid format for MAC address",
        regexp = "^((([0-9A-Fa-f]{2}:){5})|(([0-9A-Fa-f]{2}-){5}))[0-9A-Fa-f]{2}\$"
    )
    var macAddress: String,
    @field:NotNull(message = "Parameters is required field")
    var list: MutableList<NestedTestListItemRequest>
) : Request(interpolator)

class NestedTestListItemRequest(
    @JsonProperty("Num")
    @field:NotNull(message = "Num is required field")
    val number: Int,
    @JsonProperty("Value")
    @field:Size(message = "Invalid length of Value", min = 5, max = 5)
    @field:NotNull(message = "Value is required field")
    val value: String
) : Request(interpolator)
