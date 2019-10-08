package com.raywenderlich.emitron.model

/**
 * Permission tags
 */
enum class PermissionTag(val param: String) {
  /**
   * Beginner
   */
  StreamBeginner("stream-beginner-videos"),
  /**
   * Professional
   */
  StreamProfessional("stream-professional-videos"),
  /**
   * Download allowed
   */
  Download("download-videos");

  companion object {
    /**
     * Map of all [PermissionTag]
     */
    internal val map: Map<String, PermissionTag> = values().associateBy(PermissionTag::name)

    /**
     * @param type String value of [PermissionTag]
     *
     * @return [PermissionTag]
     */
    fun fromValue(type: String?): PermissionTag? = type?.let { map[it.capitalize()] }
  }
}
