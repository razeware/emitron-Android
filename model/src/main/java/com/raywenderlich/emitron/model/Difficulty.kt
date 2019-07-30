package com.raywenderlich.emitron.model

import androidx.annotation.StringRes

/**
 * Difficulty level for content
 *
 * @param resId Resource if for [Difficulty]
 */
enum class Difficulty(@StringRes val resId: Int = -1) {
  Beginner(R.string.difficulty_beginner),
  Advanced(R.string.difficulty_advanced),
  Intermediate(R.string.difficulty_intermediate);

  companion object {
    /**
     * Map of all [Difficulty]
     */
    internal val map = values().associateBy(Difficulty::name)

    /**
     * @param type String value of [Difficulty]
     *
     * @return [Difficulty]
     */
    fun fromValue(type: String?): Difficulty? = type?.let { map[it.capitalize()] }
  }
}
