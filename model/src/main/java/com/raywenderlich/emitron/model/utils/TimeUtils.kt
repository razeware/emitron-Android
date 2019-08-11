package com.raywenderlich.emitron.model.utils

import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter


object TimeUtils {

  sealed class Day {
    object Today : Day()
    object Yesterday : Day()
    object None : Day()
    data class Formatted(val readableDate: String) : Day()
  }

  fun toReadableDate(timestamp: String, shortReleaseDate: Boolean, today: LocalDateTime): Day {
    val date = OffsetDateTime.parse(timestamp)
    return when {
      isToday(date, today) -> Day.Today
      isYesterday(date, today) -> Day.Yesterday
      else -> {
        val pattern = if (shortReleaseDate) {
          "MMM d"
        } else {
          "MMM d yyyy"
        }
        val formatter = DateTimeFormatter.ofPattern(pattern)
        Day.Formatted(date.format(formatter))
      }
    }
  }

  fun toHoursAndMinutes(seconds: Long): Pair<Long, Long> {
    val duration = Duration.ofSeconds(seconds)
    val hours = duration.toHours()
    return hours to duration.toMinutes() - hours * 60
  }

  fun toHoursAndMinutesAndSeconds(seconds: Long): Triple<Long, Long, Long> {
    val duration = Duration.ofSeconds(seconds)
    val hours = duration.toHours()
    val minutes = duration.toMinutes()
    val seconds = duration.toMillis() / 1000
    return Triple(hours, (minutes - hours * 60), (seconds - minutes * 60))
  }

  /**
   * @return true if the supplied when is today else false
   */
  private fun isToday(date: OffsetDateTime, today: LocalDateTime): Boolean {
    return checkDay(date, 0, today)
  }

  /**
   * @return true if the supplied when is yesterday else false
   */
  private fun isYesterday(date: OffsetDateTime, today: LocalDateTime): Boolean {
    return checkDay(date, -1, today)
  }

  private fun checkDay(date: OffsetDateTime, offset: Long, today: LocalDateTime): Boolean {

    val thenYear = date.year
    val thenMonth = date.month
    val thenMonthDay = date.dayOfMonth


    val offsetToday = today.plusDays(offset)

    return (thenYear == offsetToday.year && thenMonth == offsetToday.month && thenMonthDay == offsetToday.dayOfMonth)
  }
}
