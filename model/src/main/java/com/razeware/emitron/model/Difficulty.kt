package com.razeware.emitron.model

/**
 * Difficulty level for content
 */
enum class Difficulty {
  /**
   * Beginner
   */
  Beginner,
  /**
   * Advanced
   */
  Advanced,
  /**
   * Intermediate
   */
  Intermediate;

  /**
   * Convert the [Difficulty] name to lowercase for use in post requests or to save to db
   */
  fun toRequestFormat(): String = this.name.toLowerCase()

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
