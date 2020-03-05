package com.razeware.emitron.ui.player

import android.app.Activity
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector

/**
 * Player configuration manager to update subtitles, playback quality and playback speed
 */
class PlayerConfigManager(
  private val trackSelector: DefaultTrackSelector,
  private val mediaPlayer: Player?
) {

  /**
   * Enable/Disable subtitles
   *
   * @param subtitleLanguage true if subtitles enabled, else false
   */
  fun updateSubtitles(subtitleLanguage: String) {
    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
    if (null != mappedTrackInfo) {
      val builder = trackSelector.buildUponParameters()
      for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
          if (subtitleLanguage.isEmpty()) {
            builder.clearSelectionOverrides(rendererIndex)
          } else {
            val subtitleOverride =
              getPlaybackSubtitleOverride(rendererIndex, mappedTrackInfo, subtitleLanguage)
            if (null != subtitleOverride) {
              builder.setSelectionOverride(
                rendererIndex,
                mappedTrackInfo.getTrackGroups(rendererIndex),
                subtitleOverride
              )
            }
          }
        }
      }
      trackSelector.setParameters(builder)
    }
  }

  /**
   * Update playback quality
   *
   * @param quality [PlayerFragment.playerPlaybackQualityOptions]
   */
  fun updatePlaybackQuality(quality: Int) {
    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
    if (null != mappedTrackInfo) {
      val builder = trackSelector.buildUponParameters()
      for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
          if (quality == PlayerFragment.defaultPlaybackQuality) {
            builder.clearSelectionOverrides(rendererIndex)
          } else {
            val qualityOverride =
              getPlaybackQualityOverride(rendererIndex, mappedTrackInfo, quality)
            if (null != qualityOverride) {
              builder.setSelectionOverride(
                rendererIndex,
                mappedTrackInfo.getTrackGroups(rendererIndex),
                qualityOverride
              )
            }
          }
        }
      }
      trackSelector.setParameters(builder)
    }
  }

  /**
   * Update default playback settings
   *
   * @param activity Context
   * @param speed [PlayerFragment.playerPlaybackSpeedOptions]
   * @param quality [PlayerFragment.playerPlaybackQualityOptions]
   * @param subtitleLanguage [PlayerFragment.playerSubtitleLanguageOptions]
   */
  fun setDefaultSettings(
    activity: Activity,
    speed: Float,
    quality: Int,
    subtitleLanguage: String
  ) {

    val builder = trackSelector.buildUponParameters()
    builder.setViewportSizeToPhysicalDisplaySize(activity, false)

    val mappedTrackInfo = trackSelector.currentMappedTrackInfo
    if (null != mappedTrackInfo) {

      for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        builder.clearSelectionOverrides(rendererIndex)
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
          val qualityOverride = getPlaybackQualityOverride(rendererIndex, mappedTrackInfo, quality)
          if (null != qualityOverride) {
            builder.setSelectionOverride(
              rendererIndex,
              mappedTrackInfo.getTrackGroups(rendererIndex),
              qualityOverride
            )
          }
        }
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
          val subtitleOverride =
            getPlaybackSubtitleOverride(rendererIndex, mappedTrackInfo, subtitleLanguage)
          if (null != subtitleOverride) {
            builder.setSelectionOverride(
              rendererIndex,
              mappedTrackInfo.getTrackGroups(rendererIndex),
              subtitleOverride
            )
          }
        }
      }
    }

    trackSelector.setParameters(builder)

    val playbackParameters = PlaybackParameters(speed)
    mediaPlayer?.playbackParameters = playbackParameters
  }

  private fun getPlaybackQualityOverride(
    rendererIndex: Int,
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    quality: Int
  ): DefaultTrackSelector.SelectionOverride? {
    val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
    for (groupIndex in 0 until trackGroups.length) {
      val group = trackGroups.get(groupIndex)
      for (trackIndex in 0 until group.length) {
        if (group.getFormat(trackIndex).height == quality) {
          if (mappedTrackInfo.getTrackSupport(
              rendererIndex,
              groupIndex,
              trackIndex
            ) == RendererCapabilities.FORMAT_HANDLED
          ) {
            return DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex)
          }
        }
      }
    }
    return null
  }

  private fun getPlaybackSubtitleOverride(
    rendererIndex: Int,
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    language: String
  ): DefaultTrackSelector.SelectionOverride? {
    val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
    for (groupIndex in 0 until trackGroups.length) {
      val group = trackGroups.get(groupIndex)
      for (trackIndex in 0 until group.length) {
        if (group.getFormat(trackIndex).language.equals(language, true)) {
          if (mappedTrackInfo.getTrackSupport(
              rendererIndex,
              groupIndex,
              trackIndex
            ) == RendererCapabilities.FORMAT_HANDLED
          ) {
            return DefaultTrackSelector.SelectionOverride(groupIndex, trackIndex)
          }
        }
      }
    }
    return null
  }

  /**
   * Update playback speed
   */
  fun updatePlaybackSpeed(speed: Float) {
    val playbackParameters = PlaybackParameters(speed)
    mediaPlayer?.playbackParameters = playbackParameters
  }
}
