package com.razeware.emitron.model.utils

import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month

class TimeUtilsTest {

  @Test
  fun toReadableDate() {
    // Given day is today
    val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)

    // When
    val result =
      TimeUtils.toReadableDate("2019-08-11T02:00:00.000Z", false, today)

    // Day should be today
    result isEqualTo TimeUtils.Day.Today

    // When
    val result2 =
      TimeUtils.toReadableDate("2019-08-10T02:00:00.000Z", false, today)

    // Day should be yesterday
    result2 isEqualTo TimeUtils.Day.Yesterday

    // When
    val result3 =
      TimeUtils.toReadableDate("2019-08-08T02:00:00.000Z", withYear = false, today = today)

    // Day should be formatted
    result3 isEqualTo TimeUtils.Day.Formatted("Aug 8")

    // When
    val result4 =
      TimeUtils.toReadableDate("2019-08-08T02:00:00.000Z", withYear = true, today = today)

    // Day should be formatted without year if it's current year
    result4 isEqualTo TimeUtils.Day.Formatted("Aug 8")

    today.minusYears(1)

    // When
    val result5 =
      TimeUtils.toReadableDate("2018-08-08T02:00:00.000Z", withYear = true, today = today)
    result5 isEqualTo TimeUtils.Day.Formatted("Aug 8 2018")
  }

  @Test
  fun toHoursAndMinutes() {
    TimeUtils.toHoursAndMinutes(4080) isEqualTo (1L to 8L)
  }

  @Test
  fun toHoursAndMinutesAndSeconds() {
    TimeUtils.toHoursAndMinutesAndSeconds(4084) isEqualTo Triple(1L, 8L, 4L)
  }
}
