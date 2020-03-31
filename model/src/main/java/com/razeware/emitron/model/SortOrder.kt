package com.razeware.emitron.model

/**
 * Sort order for content
 */
enum class SortOrder(val param: String) {
  /**
   * Newest content
   */
  Newest("-released_at"),
  /**
   * Oldest content
   */
  Oldest("released_at"),
  /**
   * Most popular content
   */
  Popularity("-popularity");

  companion object {
    /**
     * Map of all [SortOrder]
     */
    internal val map: Map<String, SortOrder> = values().associateBy(SortOrder::name)

    /**
     * @param type String value of [SortOrder]
     *
     * @return [SortOrder]
     */
    fun fromValue(type: String?): SortOrder? = type?.let { map[it.capitalize()] }
  }
}
