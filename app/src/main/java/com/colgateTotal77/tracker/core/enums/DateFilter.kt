package com.colgateTotal77.tracker.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.R
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import java.time.DayOfWeek

data class TimeRange(var start: LocalDate?, var end: LocalDate?)

fun LocalDate.startMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
fun LocalDate.endMillis(): Long = plusDays(1).startMillis() - 1

enum class RangeKind {
    DAY,
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
    CUSTOM
}

fun TimeRange.kind(): RangeKind {
    val s = start ?: return RangeKind.CUSTOM
    val e = end ?: return RangeKind.CUSTOM

    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    return when {
        e == s -> RangeKind.DAY
        s.dayOfWeek == firstDayOfWeek && e == s.plusDays(6) -> RangeKind.WEEK
        s.dayOfMonth == 1 && e == s.plusMonths(1).minusDays(1) -> RangeKind.MONTH
        s.dayOfMonth == 1 && ( s.monthValue - 1) % 3 == 0 && e == s.plusMonths(3).minusDays(1) -> RangeKind.QUARTER
        s.dayOfYear == 1 && e == s.plusYears(1).minusDays(1) -> RangeKind.YEAR
        else -> RangeKind.CUSTOM
    }
}

fun TimeRange.move(
    direction: Long
): TimeRange {
    val s = start
    val e = end

    if (s == null || e == null) {
        val today = LocalDate.now()
        return TimeRange(today.withDayOfMonth(1), today.with(TemporalAdjusters.lastDayOfMonth()))
    }

    return when (kind()) {
        RangeKind.DAY -> TimeRange(s.plusDays(direction), e.plusDays(direction))
        RangeKind.WEEK -> TimeRange(s.plusWeeks(direction), e.plusWeeks(direction))
        RangeKind.MONTH -> s.plusMonths(direction).let { TimeRange(it, it.plusMonths(1).minusDays(1)) }
        RangeKind.QUARTER -> s.plusMonths(3 * direction).let { TimeRange(it, it.plusMonths(3).minusDays(1)) }
        RangeKind.YEAR -> s.plusYears(direction).let { TimeRange(it, it.plusYears(1).minusDays(1)) }
        RangeKind.CUSTOM -> {
            val days = ChronoUnit.DAYS.between(s, e) + 1
            TimeRange(s.plusDays(days * direction), e.plusDays(days * direction))
        }
    }
}

val dayFormatter = DateTimeFormatter.ofPattern("dd")
fun LocalDate.dayMonth(): String =
    "$dayOfMonth ${format(monthShortFormatter)}"
val dayMonthNumberFormatter = DateTimeFormatter.ofPattern("dd.MM")
val monthFormatter = DateTimeFormatter.ofPattern("LLLL")
val monthShortFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendText(ChronoField.MONTH_OF_YEAR, DateFormatSymbols.getInstance().shortMonths.withIndex().associate { (i, name) -> i + 1L to name })
    .toFormatter()

val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy")

val yearSmallFormatter = DateTimeFormatter.ofPattern("yy")

fun LocalDate.weekFormatter(): String {
    val today = LocalDate.now()

    val weekStart = this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekEnd = weekStart.plusDays(6)

    val range = "${weekStart.format(dayFormatter)} - ${weekEnd.format(dayFormatter)}"

    var label = "$range\n${weekStart.format(monthShortFormatter)}"

    if (this.year != today.year) label += " ${this.format(yearSmallFormatter)}"

    return label
}

val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yy")

@Composable
fun TimeRange.label(): String {
    val s = start
    val e = end
    if (s == null) return if (e == null) stringResource(R.string.date_all_time) else stringResource(R.string.date_until, e.format(shortDateFormatter))
    if (e == null) return stringResource(R.string.date_from, s.format(shortDateFormatter))

    return when (kind()) {
        RangeKind.DAY -> s.dayMonth()
        RangeKind.MONTH -> s.format(monthFormatter).replaceFirstChar { it.titlecase(LocalLocale.current.platformLocale) }
        RangeKind.QUARTER -> "${s.format(monthShortFormatter)}-${e.format(monthShortFormatter)}"
        RangeKind.YEAR -> s.year.toString()
        RangeKind.WEEK, RangeKind.CUSTOM ->
            if (s.month == e.month && s.year == e.year) "${s.dayOfMonth}-${e.dayMonth()}"
            else "${s.format(dayMonthNumberFormatter)} - ${e.format(dayMonthNumberFormatter)}"
    }
}

fun TimeRange.yearLabel(): String? {
    val s = start ?: return null
    val e = end ?: return null
    if (kind() == RangeKind.YEAR) return null

    return when {
        s.year != e.year -> "${s.year}-${e.year}"
        s.year != LocalDate.now().year -> s.year.toString()
        else -> null
    }
}

enum class DateSwitchers(val labelRes: Int) {
    Month(R.string.date_switcher_month),
    Week(R.string.date_switcher_week),
    Day(R.string.date_switcher_day),
}

fun DateSwitchers.getStartDate(): LocalDate {
    val today = LocalDate.now()
    return when (this) {
        DateSwitchers.Day -> today.minusDays(1)
        DateSwitchers.Week -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        DateSwitchers.Month -> today.withDayOfMonth(1)
    }
}