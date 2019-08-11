package com.raywenderlich.emitron.model

import com.google.common.truth.Truth.assertThat
import com.raywenderlich.emitron.model.utils.TimeUtils
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month

class AttributesTest {

  @Test
  fun getReadableReleasedAt() {
    val attributes = Attributes()
    val today = LocalDateTime.of(2019, Month.AUGUST, 11, 2, 0, 0)
    assertThat(attributes.getReadableReleasedAt(true)).isEqualTo(TimeUtils.Day.None)

    val attributes1 = Attributes(releasedAt = "2019-08-11T02:00:00.000Z")
    assertThat(attributes1.getReadableReleasedAt(true, today))
      .isEqualTo(TimeUtils.Day.Today)

    val attributes2 = Attributes(releasedAt = "2019-08-10T02:00:00.000Z")
    assertThat(attributes2.getReadableReleasedAt(true, today))
      .isEqualTo(TimeUtils.Day.Yesterday)

    val attributes3 = Attributes(releasedAt = "2019-08-08T00:00:00.000Z")
    assertThat(attributes3.getReadableReleasedAt(true, today))
      .isEqualTo(TimeUtils.Day.Formatted("Aug 8"))
  }

  @Test
  fun getDurationHoursAndMinutes() {
    val attributes = Attributes()
    assertThat(attributes.getDurationHoursAndMinutes()).isEqualTo(0L to 0L)

    val attributes2 = Attributes(duration = 4080)
    assertThat(attributes2.getDurationHoursAndMinutes()).isEqualTo(1L to 8L)
  }

  @Test
  fun getContentType() {
    val attributes = Attributes(contentType = "screencast")
    assertThat(attributes.getContentType()).isEquivalentAccordingToCompareTo(ContentType.Screencast)
  }

  @Test
  fun getDifficulty() {
    val attributes = Attributes(difficulty = "advanced")
    assertThat(attributes.getDifficulty()).isEquivalentAccordingToCompareTo(Difficulty.Advanced)
  }

  @Test
  fun isLevelArchived() {
    val attributes = Attributes(level = "archived")
    assertThat(attributes.isLevelArchived()).isTrue()

    val attributes2 = Attributes(level = "beta")
    assertThat(attributes2.isLevelArchived()).isFalse()
  }

  @Test
  fun getPercentComplete() {
    val attributes = Attributes(percentComplete = null)
    assertThat(attributes.getPercentComplete()).isEqualTo(0)

    val attributes2 = Attributes(percentComplete = 10.0)
    assertThat(attributes2.getPercentComplete()).isEqualTo(10)
  }
}
