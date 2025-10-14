package com.hisaabi.hisaabi_kmp.home.dashboard

import kotlinx.datetime.*

/**
 * Helper to calculate date ranges for different intervals
 */
object DateRangeHelper {
    
    data class DateRange(val fromMilli: Long, val toMilli: Long)
    
    fun getDateRange(interval: IntervalEnum): DateRange {
        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        return when (interval) {
            IntervalEnum.LAST_7_DAYS -> {
                val from = today.minus(7, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault())
                DateRange(from.toEpochMilliseconds(), now.toEpochMilliseconds())
            }
            IntervalEnum.LAST_15_DAYS -> {
                val from = today.minus(15, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault())
                DateRange(from.toEpochMilliseconds(), now.toEpochMilliseconds())
            }
            IntervalEnum.THIS_MONTH -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                DateRange(startOfMonth.toEpochMilliseconds(), now.toEpochMilliseconds())
            }
            IntervalEnum.LAST_MONTH -> {
                val lastMonth = today.minus(1, DateTimeUnit.MONTH)
                val startOfLastMonth = LocalDate(lastMonth.year, lastMonth.month, 1)
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                val endOfLastMonth = LocalDate(lastMonth.year, lastMonth.month, lastMonth.month.length(isLeapYear(lastMonth.year)))
                    .atTime(23, 59, 59)
                    .toInstant(TimeZone.currentSystemDefault())
                DateRange(startOfLastMonth.toEpochMilliseconds(), endOfLastMonth.toEpochMilliseconds())
            }
            IntervalEnum.THIS_YEAR -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                DateRange(startOfYear.toEpochMilliseconds(), now.toEpochMilliseconds())
            }
            IntervalEnum.LAST_YEAR -> {
                val lastYear = today.year - 1
                val startOfLastYear = LocalDate(lastYear, 1, 1)
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                val endOfLastYear = LocalDate(lastYear, 12, 31)
                    .atTime(23, 59, 59)
                    .toInstant(TimeZone.currentSystemDefault())
                DateRange(startOfLastYear.toEpochMilliseconds(), endOfLastYear.toEpochMilliseconds())
            }
            IntervalEnum.ALL_RECORD -> {
                DateRange(0, now.toEpochMilliseconds())
            }
        }
    }
    
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}

