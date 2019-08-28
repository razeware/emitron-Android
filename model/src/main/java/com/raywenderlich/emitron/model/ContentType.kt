package com.raywenderlich.emitron.model;

/**
 * @param type String value of [ContentType]
 *
 * @return [ContentType]
 */
enum class ContentType {
  /**
   * Collection
   */
  Collection,
  /**
   * Screencast
   */
  Screencast;

  /**
   * @return True if the current content is screencast, otherwise False
   */
  fun isScreenCast(): Boolean = this == Screencast

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

    fun getAllowedContentType() = values().map { it.name.toLowerCase() }.toTypedArray()
  }
}
