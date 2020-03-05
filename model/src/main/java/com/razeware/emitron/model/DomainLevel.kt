package com.razeware.emitron.model

/**
 * Domain level for content
 */
enum class DomainLevel {
  Production,
  Beta,
  Archived;

  companion object {
    /**
     * Map of all [DomainLevel]
     */
    internal val map: Map<String, DomainLevel> = values().associateBy(DomainLevel::name)

    /**
     * @param type String value of [DomainLevel]
     *
     * @return [DomainLevel]
     */
    fun fromValue(type: String?): DomainLevel? = type?.let { map[it.capitalize()] }
  }
}
