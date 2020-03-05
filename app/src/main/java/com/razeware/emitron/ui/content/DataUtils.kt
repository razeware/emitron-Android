package com.razeware.emitron.ui.content

import android.content.Context
import com.razeware.emitron.R
import com.razeware.emitron.model.ContentType
import com.razeware.emitron.model.Data
import com.razeware.emitron.model.Difficulty
import com.razeware.emitron.model.utils.TimeUtils
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime

/**
 * [Data.attributes] contains release date, difficulty, and content type
 *
 * @return Readable formatted string of release date, difficulty and content type
 */
fun Data.getReadableReleaseAtWithTypeAndDuration(
  context: Context,
  withDifficulty: Boolean = false,
  withYear: Boolean = true
): String {
  with(context) {

    val releasedAt =
      when (val day = getReleasedAt(withYear, LocalDateTime.now(Clock.systemUTC()))) {
        is TimeUtils.Day.Today -> getString(R.string.today)
        is TimeUtils.Day.Yesterday -> getString(R.string.yesterday)
        is TimeUtils.Day.Formatted -> day.readableDate
        else -> ""
      }

    val contentTypeString = when (getContentType()) {
      ContentType.Collection -> getString(R.string.content_type_video_course)
      ContentType.Screencast -> getString(R.string.content_type_screencast)
      else ->
        ""
    }

    val (hrs, mins) = getDurationHoursAndMinutes()

    val durationHrs = if (hrs > 0) {
      "${resources.getQuantityString(R.plurals.hours, hrs.toInt(), hrs)} "
    } else {
      ""
    }

    val durationMins = if (mins > 0) {
      resources.getQuantityString(R.plurals.minutes, mins.toInt(), mins)
    } else {
      ""
    }

    if (withDifficulty) {

      val difficultyString = when (getDifficulty()) {
        Difficulty.Advanced -> getString(R.string.difficulty_advanced)
        Difficulty.Beginner -> getString(R.string.difficulty_beginner)
        Difficulty.Intermediate -> getString(R.string.difficulty_intermediate)
        else -> ""
      }

      return getString(
        R.string.label_release_difficulty_type_duration,
        releasedAt,
        difficultyString,
        contentTypeString,
        durationHrs,
        durationMins
      )

    }

    return getString(
      R.string.label_release_type_duration,
      releasedAt,
      contentTypeString,
      durationHrs,
      durationMins
    )
  }
}

/**
 * @return Readable formatted string of contributors
 */
fun Data.getReadableContributors(context: Context): String {
  with(context) {
    val contributors: String? = getContributors()

    return if (contributors.isNullOrBlank()) {
      ""
    } else {
      getString(
        R.string.contributors, getContributors()
      )
    }
  }
}

/**
 * @return Domain of content, or `Multiplatform` if content has multiple domains
 */
fun Data.getReadableDomain(context: Context): String? {
  with(context) {
    return if (getDomainIds().size > 1) {
      getString(R.string.label_multi_platform)
    } else {
      getDomain() ?: ""
    }
  }
}
