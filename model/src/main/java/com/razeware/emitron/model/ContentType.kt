package com.razeware.emitron.model

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
  Episode,

  /**
   * Profession content
   */
  Professional;

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
      values().filter { !it.isEpisode() && !it.isProfessional() }.map { it.name.toLowerCase() }.toTypedArray()

    /**
     * Allowed downloadable [ContentType]s
     *
     * @return List of [ContentType]
     */
    fun getAllowedDownloadTypes(): Array<String> =
      values().filter { !it.isCollection() && !it.isProfessional() }.map { it.name.toLowerCase() }.toTypedArray()

    /**
     * Allowed content filters [ContentType]s
     *
     * @return List of [ContentType]
     */
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
 * @return True if the current content is professional, otherwise False
 */
fun ContentType?.isProfessional(): Boolean = this == ContentType.Professional

/**
 * Convert the [ContentType] name to lowercase for use in post requests or to save to db
 */
fun ContentType.toRequestFormat(): String = this.name.toLowerCase()
