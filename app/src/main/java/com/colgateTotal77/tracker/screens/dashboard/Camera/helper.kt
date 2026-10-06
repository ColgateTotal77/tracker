package com.colgateTotal77.tracker.screens.dashboard.Camera

import com.colgateTotal77.tracker.screens.dashboard.Camera.fiscal.FiscalQrParams
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.days

fun isRecentReceipt(params: FiscalQrParams?): Boolean {
    if (params == null) return false
    val date = params.date.filter(Char::isDigit)
    val time = params.time.orEmpty().filter(Char::isDigit)
    if (date.length != 8 || time.length !in listOf(0, 2, 4, 6)) return false

    val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.ROOT).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("Europe/Kiev")
    }
    val position = ParsePosition(0)
    val input = date + time.padEnd(6, '0')
    val timestamp = formatter.parse(input, position)?.time ?: return false
    if (position.index != input.length) return false
    val age = System.currentTimeMillis() - timestamp
    return age in 0..2.days.inWholeMilliseconds
}
