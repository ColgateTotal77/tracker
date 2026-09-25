package com.colgateTotal77.tracker.core.enums


enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "\u20ac", "Euro"),
    GBP("GBP", "\u00a3", "British Pound"),
    JPY("JPY", "\u00a5", "Japanese Yen"),
    UAH("UAH", "\u20b4", "Ukrainian Hryvnia");

    val dropdownText: String
        get() = "$code ($symbol) - $displayName"
}