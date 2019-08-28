package com.raywenderlich.emitron.ui.content

import android.content.Context
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.model.ContentType
import com.raywenderlich.emitron.model.Data
import com.raywenderlich.emitron.model.Difficulty
import com.raywenderlich.emitron.model.utils.TimeUtils

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

    val releasedAt = when (val day = getReleasedAt(withYear)) {
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

    val (hrs, mins) = getDuration()

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
