package com.raywenderlich.emitron.model

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
  Screencast,
  /**
   * Episode
   */
  Episode;

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
    fun getAllowedContentTypes(): Array<String> =
      values().filter { !it.isEpisode() }.map { it.name.toLowerCase() }.toTypedArray()
    fun getFilterContentTypes(): List<ContentType> =
      values().filter { !it.isEpisode() }
  }
}

/**
 * @return True if the current content is screencast, otherwise False
 */
fun ContentType?.isScreencast(): Boolean = this == ContentType.Screencast

/**
 * @return True if the current content is collection, otherwise False
 */
fun ContentType?.isCollection(): Boolean = this == ContentType.Collection

/**
 * @return True if the current content is episode, otherwise False
 */
fun ContentType?.isEpisode(): Boolean = this == ContentType.Episode

/**
 * Convert the [ContentType] name to lowercase for use in post requests or to save to db
 */
fun ContentType.toRequestFormat(): String = this.name.toLowerCase()
