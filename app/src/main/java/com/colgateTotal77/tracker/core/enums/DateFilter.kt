package com.colgateTotal77.tracker.core.enums

import java.util.Calendar

data class TimeRange(val start: Long?, val end: Long?)

enum class DateFilter(val label: String) {
    AllTime("All Time"),
    Month("Current month"),
    PrevMonth("Previous month"),
    SixMonth("Last 6 months"),
    PrevSixMonth("Previous 6 months"),
    Year("Current year"),
    PrevYear("Previous year"),
}

fun DateFilter.toTimeRange(): TimeRange {
    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis

    fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    fun Calendar.setToEndOfDay() {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    var startTime: Long? = 0
    var endTime: Long? = now

    when (this) {
        DateFilter.AllTime -> {
            startTime = null
            endTime = null
        }
        DateFilter.Month -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.setToStartOfDay()
            startTime = calendar.timeInMillis
        }
        DateFilter.PrevMonth -> {
            calendar.add(Calendar.MONTH, -1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.setToStartOfDay()
            startTime = calendar.timeInMillis

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.setToEndOfDay()
            endTime = calendar.timeInMillis
        }
        DateFilter.SixMonth -> {
            calendar.add(Calendar.MONTH, -6)
            calendar.setToStartOfDay()
            startTime = calendar.timeInMillis
        }
        DateFilter.PrevSixMonth -> {
            calendar.add(Calendar.MONTH, -6)
            calendar.setToEndOfDay()
            endTime = calendar.timeInMillis

            calendar.add(Calendar.MONTH, -6)
            calendar.setToStartOfDay()
            startTime = calendar.timeInMillis
        }
        DateFilter.Year -> {
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.setToStartOfDay()
            startTime = calendar.timeInMillis
        }
        DateFilter.PrevYear -> {
            calendar.add(Calendar.YEAR, -1)

            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.setToStartOfDay()
            startTime = calendar.timeInMillis

            calendar.set(Calendar.MONTH, Calendar.DECEMBER)
            calendar.set(Calendar.DAY_OF_MONTH, 31)
            calendar.setToEndOfDay()
            endTime = calendar.timeInMillis
        }
    }

    return TimeRange(start = startTime, end = endTime)
}