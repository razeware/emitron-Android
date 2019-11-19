package com.razeware.emitron.model

/**
 * Download quality
 */
enum class DownloadQuality(
  /** pref value */
  val pref: String,
  /** api value */
  val kind: String
) {
  /**
   * HD
   */
  HD("hd", "hd_video_file"),
  /**
   * SD
   */
  SD("sd", "sd_video_file");

  companion object {
    /**
     * Map of all [DownloadQuality]
     */
    internal val prefMap = values().associateBy(DownloadQuality::pref)

    internal val kindMap = values().associateBy(DownloadQuality::kind)

    /**
     * @param pref [DownloadQuality.pref] from preference
     *
     * @return [DownloadQuality]
     */
    fun fromPref(pref: String?): DownloadQuality? = pref?.let { prefMap[it] }

    /**
     * @param kind [DownloadQuality.kind] from API
     *
     * @return [DownloadQuality]
     */
    fun fromKind(kind: String?): DownloadQuality? = kind?.let { kindMap[it] }
  }
}

/**
 * true if Download quality is [DownloadQuality.HD] else false
 */
fun DownloadQuality?.isHd(): Boolean = this == DownloadQuality.HD

/**
 * true if Download quality is [DownloadQuality.SD] else false
 */
fun DownloadQuality?.isSd(): Boolean = this == DownloadQuality.SD

