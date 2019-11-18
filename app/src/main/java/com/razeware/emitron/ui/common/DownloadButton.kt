package com.razeware.emitron.ui.common

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.razeware.emitron.R
import com.razeware.emitron.model.*
import com.razeware.emitron.utils.extensions.toVisibility

/**
 * Custom Button to handle download state
 */
class DownloadButton : FrameLayout {

  private lateinit var downloadButton: MaterialButton
  private lateinit var downloadStopButton: MaterialButton
  private lateinit var progressDownloadingPending: ProgressBar
  private lateinit var progressDownload: ProgressBar

  @JvmOverloads
  constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    init(attrs)
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
  ) {
    init(attrs)
  }

  private fun setupAttributes(attrs: AttributeSet?) {
    // Obtain a typed array of attributes
    val attr = context.theme.obtainStyledAttributes(
      attrs,
      R.styleable.DownloadButton, 0, 0
    )
    // Extract custom attributes into member variables
    try {
      val downloadButtonIcon = attr.getDrawable(
        R.styleable.DownloadButton_downloadIcon
      )
      downloadButton.icon = downloadButtonIcon
      val downloadButtonIconTint = attr.getColorStateList(
        R.styleable.DownloadButton_downloadIconTint
      )
      downloadButton.iconTint = downloadButtonIconTint

      val downloadStopButtonIcon = attr.getDrawable(
        R.styleable.DownloadButton_downloadStopIcon
      )
      downloadStopButton.icon = downloadStopButtonIcon

      val downloadStopButtonIconTint = attr.getColorStateList(
        R.styleable.DownloadButton_downloadStopIconTint
      )
      downloadStopButton.iconTint = downloadStopButtonIconTint

      val progressDrawable = attr.getResourceId(
        R.styleable.DownloadButton_progressDrawable,
        R.drawable.progress_drawable
      )
      progressDownload.progressDrawable = ContextCompat.getDrawable(
        context,
        progressDrawable
      )
    } finally {
      // TypedArray objects are shared and must be recycled.
      attr.recycle()
    }
  }


  /**
   * Initialize view
   */
  private fun init(attrs: AttributeSet?) {

    val root = inflate(context, R.layout.layout_button_download, this)

    with(root) {
      downloadButton = findViewById(R.id.button_download)
      downloadStopButton = findViewById(R.id.button_download_stop)
      progressDownloadingPending = findViewById(R.id.progress_download_pending)
      progressDownload = findViewById(R.id.progress_download)
    }

    setupAttributes(attrs)

    downloadButton.toVisibility(true)
    downloadStopButton.toVisibility(false)
    progressDownloadingPending.toVisibility(false)
    progressDownload.toVisibility(false)
  }

  /**
   * Update download state
   *
   * @param download Download
   */
  fun updateDownloadState(download: Download?) {
    downloadButton.iconTint = ContextCompat.getColorStateList(context, R.color.colorIcon)
    when {
      download.isDownloaded() -> {
        downloadStopButton.toVisibility(false)
        progressDownloadingPending.toVisibility(false)
        progressDownload.toVisibility(false)
        downloadButton.toVisibility(true)
        downloadButton.iconTint = ContextCompat.getColorStateList(context, R.color.colorPrimary)
      }
      download.isDownloading() -> {
        downloadButton.toVisibility(false)
        downloadStopButton.toVisibility(true)
        progressDownloadingPending.toVisibility(false)
        progressDownload.toVisibility(true)
        progressDownload.progress = download.getProgress()
      }
      download.isPending() -> {
        downloadButton.toVisibility(false)
        progressDownloadingPending.toVisibility(true)
        downloadStopButton.toVisibility(true)
      }
      download.isPaused() -> {
        downloadButton.toVisibility(true)
        progressDownloadingPending.toVisibility(false)
        downloadStopButton.toVisibility(false)
        progressDownload.toVisibility(false)
      }
      download.isFailed() -> {
        downloadButton.toVisibility(true)
      }
      else -> {
        downloadButton.toVisibility(true)
        progressDownloadingPending.toVisibility(false)
        downloadStopButton.toVisibility(false)
        progressDownload.toVisibility(false)
      }
    }
  }
}
