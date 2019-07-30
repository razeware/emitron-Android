package com.raywenderlich.emitron.model

/**
 * Data types
 */
enum class DataType {
  Domains,
  Bookmarks,
  Progressions,
  Categories,
  Groups,
  Contents;

  /**
   * Convert the [DataType] name to lowercase for use in post requests
   */
  fun toRequestFormat() = this.name.toLowerCase()

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
