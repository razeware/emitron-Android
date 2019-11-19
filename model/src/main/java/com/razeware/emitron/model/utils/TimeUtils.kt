package com.razeware.emitron.model.utils

import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * Helper functions to parse time
 */
object TimeUtils {

  /**
   * Data object for representing a Day value
   */
  sealed class Day {
    /** Today */
    object Today : Day()

    /** Yesterday */
    object Yesterday : Day()

    /** No day/ Empty value */
    object None : Day()

    /** A formatted day */
    data class Formatted(
      /** Day in readable format*/
      val readableDate: String
    ) : Day()
  }

  /**
   * Format ISO time stamp to readable date
   *
   * @param timestamp timestamp string for parsing
   * @param withYear should we parse without year
   * @param today LocalDateTime instance for parsing
   *
   * @return [Day]
   */
  fun toReadableDate(timestamp: String, withYear: Boolean, today: LocalDateTime): Day {
    val date = OffsetDateTime.parse(timestamp)
    return when {
      isToday(date, today) -> Day.Today
      isYesterday(date, today) -> Day.Yesterday
      else -> {
        val isCurrentYear = date.year == today.year
        val pattern = if (withYear && !isCurrentYear) {
          "MMM d yyyy"
        } else {
          "MMM d"
        }
        val formatter = DateTimeFormatter.ofPattern(pattern)
        Day.Formatted(date.format(formatter))
      }
    }
  }

  /**
   * Convert seconds to hours/minutes
   *
   * @param timeInSeconds time in seconds
   *
   * @return [Pair] of hours and minutes
   */
  fun toHoursAndMinutes(timeInSeconds: Long): Pair<Long, Long> {
    val duration = Duration.ofSeconds(timeInSeconds)
    val hours = duration.toHours()
    return hours to duration.toMinutes() - hours * 60
  }

  /**
   * Convert seconds to hours/minutes/seconds
   *
   * @param timeInSeconds time in seconds
   *
   * @return [Triple] of hours, minutes and seconds
   */
  fun toHoursAndMinutesAndSeconds(timeInSeconds: Long): Triple<Long, Long, Long> {
    val duration = Duration.ofSeconds(timeInSeconds)
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

    return (thenYear == offsetToday.year &&
        thenMonth == offsetToday.month &&
        thenMonthDay == offsetToday.dayOfMonth)
  }
}
