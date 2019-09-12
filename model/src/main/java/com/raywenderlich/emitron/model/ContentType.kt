package com.raywenderlich.emitron.model;

/**
 * Content type
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

  /**
   * Convert the [ContentType] name to lowercase for use in post requests or to save to db
   */
  fun toRequestFormat(): String = this.name.toLowerCase()

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

    /**
     * Allowed supported [ContentType]s
     *
     * If required you can filter out the [ContentType] not supported by the app
     *
     * @return List of [ContentType]
     */
    fun getAllowedContentType(): Array<String> =
      values().map { it.name.toLowerCase() }.toTypedArray()
  }
}
