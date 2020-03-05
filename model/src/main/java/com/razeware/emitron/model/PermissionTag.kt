package com.razeware.emitron.model

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
    internal val map: Map<String, PermissionTag> = values().associateBy(PermissionTag::param)

    /**
     * @param type String value of [PermissionTag]
     *
     * @return [PermissionTag]
     */
    fun fromParam(param: String?): PermissionTag? = param?.let { map[it] }
  }
}

/**
 * Is download permission tag?
 *
 * @return true if this tag is [PermissionTag.Download], else false
 */
fun PermissionTag?.isDownloadPermissionTag(): Boolean = this == PermissionTag.Download
