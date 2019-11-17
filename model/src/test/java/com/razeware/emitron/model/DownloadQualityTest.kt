package com.razeware.emitron.model

import com.razeware.emitron.model.utils.isEqualTo
import org.junit.Test

class DownloadQualityTest {

  @Test
  fun isHd() {
    val quality = DownloadQuality.SD

    quality.isHd() isEqualTo false
    quality.isSd() isEqualTo true
  }

  @Test
  fun isSd() {
    val quality = DownloadQuality.HD

    quality.isHd() isEqualTo true
    quality.isSd() isEqualTo false
  }

  @Test
  fun fromKind() {
    val quality = DownloadQuality.fromKind("hd_video_file")
    quality.isHd() isEqualTo true
    val sdQuality = DownloadQuality.fromKind("sd_video_file")
    sdQuality.isSd() isEqualTo true
  }

  @Test
  fun fromPref() {
    val quality = DownloadQuality.fromPref("hd")
    quality.isHd() isEqualTo true
    val sdQuality = DownloadQuality.fromPref("sd")
    sdQuality.isSd() isEqualTo true
  }
}
