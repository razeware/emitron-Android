package com.razeware.emitron.ui.player.cast

import com.google.android.exoplayer2.util.MimeTypes
import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.createDownload
import com.razeware.emitron.utils.isEqualTo
import org.junit.Test

class EpisodeTest {

  @Test
  fun fromData() {
    val data = createContentData(videoUrl = "videoUrl")
    Episode.fromData(data) isEqualTo Episode(
      name = "Introduction to Kotlin Lambdas",
      uri = "videoUrl",
      progress = 0,
      mimeType = MimeTypes.APPLICATION_M3U8
    )
  }

  @Test
  fun fromData_downloaded() {
    val data = createContentData(
      videoUrl = "videoUrl",
      download = createDownload().toDownloadState()
    )
    Episode.fromData(data) isEqualTo Episode(
      name = "Introduction to Kotlin Lambdas",
      uri = "videoUrl",
      progress = 0,
      mimeType = MimeTypes.APPLICATION_MP4
    )
  }
}
