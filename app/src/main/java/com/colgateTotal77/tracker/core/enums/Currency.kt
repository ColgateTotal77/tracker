package com.colgateTotal77.tracker.core.enums


enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
) {
    UAH("UAH", "\u20b4", "Ukrainian Hryvnia"),
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "\u20ac", "Euro");

    val dropdownText: String
        get() = "$code ($symbol) - $displayName"

    val dropdownTextSmall: String
        get() = "$code ($symbol)"
}