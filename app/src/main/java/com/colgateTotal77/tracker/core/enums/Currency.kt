package com.colgateTotal77.tracker.core.enums

import android.content.Context
import com.colgateTotal77.tracker.R

enum class Currency(
    val code: String,
    val symbol: String,
    val nameRes: Int,
) {
    UAH("UAH", "\u20b4", R.string.currency_uah),
    USD("USD", "$", R.string.currency_usd),
    EUR("EUR", "\u20ac", R.string.currency_eur);

    fun dropdownText(context: Context) = "$code ($symbol) - ${context.getString(nameRes)}"

    val dropdownTextSmall: String
        get() = "$code ($symbol)"
}