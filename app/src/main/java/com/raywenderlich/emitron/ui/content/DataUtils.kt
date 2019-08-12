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
 * This extension function formats release date, difficulty and content type to readable string
 */
fun Data.setReadableReleaseAtWithTypeAndDuration(
  context: Context,
  data: Data,
  withDifficulty: Boolean = false,
  shortReleaseDate: Boolean = false
) {
  with(context) {

    val releasedAt = when (val day = data.getReleasedAt(shortReleaseDate)) {
      is TimeUtils.Day.Today -> getString(R.string.today)
      is TimeUtils.Day.Yesterday -> getString(R.string.yesterday)
      is TimeUtils.Day.Formatted -> day.readableDate
      else -> ""
    }

    val contentTypeString = when (data.getContentType()) {
      ContentType.Collection -> getString(R.string.content_type_video_course)
      ContentType.Screencast -> getString(R.string.content_type_screencast)
      else ->
        ""
    }

    val (hrs, mins) = data.getDuration()

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

      val difficultyString = when (data.getDifficulty()) {
        Difficulty.Advanced -> getString(R.string.difficulty_advanced)
        Difficulty.Beginner -> getString(R.string.difficulty_beginner)
        Difficulty.Intermediate -> getString(R.string.difficulty_intermediate)
        else -> ""
      }

      releaseDateWithTypeAndDuration = getString(
        R.string.label_release_difficulty_type_duration,
        releasedAt,
        difficultyString,
        contentTypeString,
        durationHrs,
        durationMins
      )
      return
    }

    releaseDateWithTypeAndDuration = getString(
      R.string.label_release_type_duration,
      releasedAt,
      contentTypeString,
      durationHrs,
      durationMins
    )
  }
}
