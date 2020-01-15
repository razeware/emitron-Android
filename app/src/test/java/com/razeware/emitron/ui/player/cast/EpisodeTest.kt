package com.razeware.emitron.ui.player.cast

import com.google.android.exoplayer2.util.MimeTypes
import com.razeware.emitron.data.createContentData
import com.razeware.emitron.data.createDownload
import com.razeware.emitron.data.withProgression
import com.razeware.emitron.data.withRelatedProgression
import com.razeware.emitron.utils.isEqualTo
import org.junit.Test

class EpisodeTest {

  @Test
  fun getProgressInMillis() {
    val data = createContentData(
      videoUrl = "videoUrl",
      progression = withRelatedProgression(withProgression(finished = false, progress = 10))
    )
    Episode.fromData(data).getProgressInMillis() isEqualTo 10000
  }

  @Test
  fun getProgressInMillis_finished() {
    val data = createContentData(
      videoUrl = "videoUrl",
      progression = withRelatedProgression(withProgression(finished = true, progress = 100))
    )
    Episode.fromData(data).getProgressInMillis() isEqualTo 0
  }


  @Test
  fun fromData() {
    val data = createContentData(videoUrl = "videoUrl")
    Episode.fromData(data) isEqualTo Episode(
      name = "Introduction to Kotlin Lambdas",
      uri = "videoUrl",
      progress = 0,
      isCompleted = true,
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
      isCompleted = true,
      mimeType = MimeTypes.APPLICATION_MP4
    )
  }
}
