package com.raywenderlich.emitron.model

/**
 * Data types
 */
enum class DataType {
  /** Domains */
  Domains,
  /** Bookmarks */
  Bookmarks,
  /** Progressions */
  Progressions,
  /** Categories */
  Categories,
  /** Groups */
  Groups,
  /**
   * Search
   *
   * Local data type to add search filter
   */
  Search,
  /**
   * Sort
   *
   * Local data type to add sort filter
   */
  Sort,
  /** Contents */
  Contents;

  /**
   * Convert the [DataType] name to lowercase for use in post requests
   */
  fun toRequestFormat(): String = this.name.toLowerCase()

  companion object {

    /**
     * Map of all [DataType]
     */
    internal val map = values().associateBy(DataType::name)

    /**
     * @param type String value of [DataType]
     *
     * @return [DataType]
     */
    fun fromValue(type: String?): DataType? = type?.let { map[it.capitalize()] }
  }
}
