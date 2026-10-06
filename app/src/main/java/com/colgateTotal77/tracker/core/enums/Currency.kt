package com.colgateTotal77.tracker.core.enums


enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
) {
    USD("USD", "$", "US Dollar"),
    UAH("UAH", "\u20b4", "Ukrainian Hryvnia");

    val dropdownText: String
        get() = "$code ($symbol) - $displayName"
}