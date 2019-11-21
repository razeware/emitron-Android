package com.razeware.emitron.model

/**
 * Data types
 */
enum class DataType(val jsonName: String) {
  /** Domains */
  Domains("domains"),
  /** Bookmarks */
  Bookmarks("bookmarks"),
  /** Progressions */
  Progressions("progressions"),
  /** Categories */
  Categories("categories"),
  /** Groups */
  Groups("groups"),
  /** Contents */
  Contents("contents"),
  /** Permissions */
  Permissions("permissions"),
  /**
   * WatchStats
   */
  WatchStats("watch_stats");

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

/**
 *  @return true if type is [DataType.Progressions], else false
 */
fun DataType?.isProgression(): Boolean = this == DataType.Progressions

/**
 *  @return true if type is [DataType.Contents], else false
 */
fun DataType?.isContent(): Boolean = this == DataType.Contents

/**
 *  @return true if type is [DataType.Domains], else false
 */
fun DataType?.isDomain(): Boolean = this == DataType.Domains

/**
 *  @return true if type is [DataType.Bookmarks], else false
 */
fun DataType?.isBookmark(): Boolean = this == DataType.Bookmarks

/**
 * Convert the [DataType] name to lowercase for use in post requests
 */
fun DataType.toRequestFormat(): String = this.jsonName
