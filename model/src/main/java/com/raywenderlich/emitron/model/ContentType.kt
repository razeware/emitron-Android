package com.raywenderlich.emitron.model;

import androidx.annotation.StringRes;

/**
 * @param type String value of [ContentType]
 *
 * @return [ContentType]
 */
enum class ContentType(@StringRes val resId: Int = 0) {
  Collection(R.string.video_course),
  Screencast(R.string.screencast);

  /**
   * @return True if the current content is screencast, otherwise False
   */
  fun isScreenCast() = this == Screencast

  companion object {

    /**
     * Map of all [ContentType]
     */
    internal val map = values().associateBy(ContentType::name)

    /**
     * @param type String value of [ContentType]
     *
     * @return [ContentType]
     */
    fun fromValue(type: String?): ContentType? = type?.let { map[it.capitalize()] }
  }
}
