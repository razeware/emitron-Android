package com.razeware.emitron.model

/**
 * Data types
 */
enum class FilterType(val type: String) {
  /** Domains */
  Domains("domains"),
  /** Categories */
  Categories("categories"),
  /** Search */
  Search("search"),
  /** Sort */
  Sort("sort"),
  /** Content Type */
  ContentType("contentType"),
  /** Difficulty */
  Difficulty("difficulty");

  companion object {

    /**
     * Map of all [DataType]
     */
    internal val map = values().associateBy(FilterType::type)

    /**
     * @param type String value of [DataType]
     *
     * @return [DataType]
     */
    fun fromType(type: String?): FilterType? = type?.let { map[it] }
  }
}

/**
 *  @return true if type is [FilterType.Domains], else false
 */
fun FilterType?.isDomain(): Boolean = this == FilterType.Domains

/**
 *  @return true if type is [FilterType.Categories], else false
 */
fun FilterType?.isCategory(): Boolean = this == FilterType.Categories

/**
 *  @return true if type is [FilterType.ContentType], else false
 */
fun FilterType?.isContentType(): Boolean = this == FilterType.ContentType

/**
 *  @return true if type is [FilterType.Difficulty], else false
 */
fun FilterType?.isDifficulty(): Boolean = this == FilterType.Difficulty

/**
 *  @return true if type is [FilterType.Search], else false
 */
fun FilterType?.isSearch(): Boolean = this == FilterType.Search

/**
 *  @return true if type is [FilterType.Sort], else false
 */
fun FilterType?.isSort(): Boolean = this == FilterType.Sort

/**
 * Convert the [DataType] name to lowercase for use in post requests
 */
fun FilterType.toRequestFormat(): String = this.type
