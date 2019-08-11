package com.raywenderlich.emitron.model

/**
 * Difficulty level for content
 *
 * @param resId Resource if for [Difficulty]
 */
enum class Difficulty {
  Beginner,
  Advanced,
  Intermediate;

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
