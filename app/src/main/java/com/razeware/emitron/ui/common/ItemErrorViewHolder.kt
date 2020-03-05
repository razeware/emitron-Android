package com.razeware.emitron.ui.common

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.databinding.ItemErrorBinding
import com.razeware.emitron.ui.content.ContentAdapter
import com.razeware.emitron.utils.UiStateManager
import com.razeware.emitron.utils.extensions.hasQ
import com.razeware.emitron.utils.extensions.isNetNotConnected
import com.razeware.emitron.utils.hasError
import com.razeware.emitron.utils.isLoading


/**
 * View holder for error
 */
class ItemErrorViewHolder(
  private val binding: ItemErrorBinding,
  private val retryCallback: () -> Unit,
  private val emptyCallback: (() -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

  init {
    binding.textViewProgress.visibility = View.GONE
    binding.buttonRetry.visibility = View.GONE
  }

  /**
   * @param uiState for this item layout
   * @param type Type for current adapter.
   */
  fun bindTo(
    uiState: UiStateManager.UiState?,
    type: ContentAdapter.Type
  ) {

    with(binding) {
      progressBar.isVisible = uiState.isLoading()
      textViewProgress.isVisible = uiState.isLoading()
      textViewError.isVisible = uiState.hasError()
      buttonRetry.isVisible = uiState.hasError()
      buttonRetry.setOnClickListener {
        if (uiState?.isEmpty() == true) {
          emptyCallback?.invoke()
        } else {
          retryCallback()
        }
      }

      buttonRetry.setIconResource(
        getRetryActionIconResource(
          type
        )
      )

      val resources = root.resources

      imageError.layoutParams.height =
        resources.getDimensionPixelSize(R.dimen.error_image_height_width)
      imageError.layoutParams.width =
        resources.getDimensionPixelSize(R.dimen.error_image_height_width)

      when (uiState) {
        UiStateManager.UiState.ERROR_CONNECTION -> {
          textViewError.text = resources.getString(R.string.error_no_internet)
          textViewErrorBody.text = resources.getString(R.string.error_no_internet_body)
          imageError.layoutParams.height =
            resources.getDimensionPixelSize(R.dimen.empty_image_height_width)
          imageError.layoutParams.width =
            resources.getDimensionPixelSize(R.dimen.empty_image_height_width)
          imageError.setImageResource(R.drawable.ic_emoji_crying)
          buttonRetry.setPadding(0)
          buttonRetry.icon = null
        }
        UiStateManager.UiState.INIT_EMPTY, UiStateManager.UiState.EMPTY -> {
          textViewError.text = getEmptyErrorForAdapterType(resources, type)
          textViewErrorBody.text = getEmptyErrorBodyForAdapterType(resources, type)
          textViewErrorBody.isVisible =
            getEmptyErrorBodyForAdapterType(resources, type).isNotEmpty()
          buttonRetry.isVisible = !type.isContentWithFilters()
          buttonRetry.text = getRetryButtonLabelForAdapterType(resources, type)
          val emptyDrawable = getEmptyDrawable(type)
          if (null != emptyDrawable) {
            imageError.setImageResource(emptyDrawable)
          } else {
            imageError.setImageDrawable(null)
          }
          imageError.isVisible = null != emptyDrawable
        }
        else -> if (root.context.isNetNotConnected()) {
          textViewError.text =
            resources.getString(R.string.error_no_internet)
        } else {
          textViewError.text =
            resources.getString(R.string.error_library_no_content)
        }
      }
    }
  }

  private fun getEmptyErrorForAdapterType(
    resources: Resources,
    type: ContentAdapter.Type
  ) =
    when (type) {
      ContentAdapter.Type.Content,
      ContentAdapter.Type.ContentWithFilters,
      ContentAdapter.Type.ContentWithSearch ->
        resources.getString(R.string.error_library_no_content)
      ContentAdapter.Type.ContentBookmarked ->
        resources.getString(R.string.title_bookmarks_empty)
      ContentAdapter.Type.ContentInProgress ->
        resources.getString(R.string.title_progressions_empty)
      ContentAdapter.Type.ContentCompleted ->
        resources.getString(R.string.title_progressions_completed_empty)
      ContentAdapter.Type.ContentDownloaded ->
        resources.getString(R.string.title_downloads_empty)
    }

  private fun getEmptyErrorBodyForAdapterType(
    resources: Resources,
    type: ContentAdapter.Type
  ): Spannable =
    when (type) {
      ContentAdapter.Type.ContentDownloaded -> {
        getEmptyErrorBodyWithImage(
          resources,
          R.string.body_downloads_empty,
          R.drawable.ic_material_icon_download
        )
      }
      ContentAdapter.Type.ContentBookmarked -> {
        getEmptyErrorBodyWithImage(
          resources,
          R.string.body_bookmarks_empty,
          R.drawable.ic_material_icon_bookmark
        )
      }
      ContentAdapter.Type.ContentWithFilters ->
        SpannableString(resources.getString(R.string.error_library_no_content_body))
      ContentAdapter.Type.ContentInProgress ->
        SpannableString(resources.getString(R.string.body_progressions_empty))
      ContentAdapter.Type.ContentCompleted ->
        SpannableString(resources.getString(R.string.body_progressions_completed_empty))
      else -> SpannableString("")
    }

  private fun getEmptyErrorBodyWithImage(
    resources: Resources,
    @StringRes messageResId: Int,
    @DrawableRes drawableId: Int
  ): Spannable {
    val body = resources.getString(messageResId)
    val imageSpanIndex = body.asSequence().indexOfFirst { it == '$' }
    val spannable: Spannable = SpannableString(body)
    val imageSpan = buildImageSpan(resources, drawableId)
    spannable.setSpan(
      imageSpan ?: "",
      imageSpanIndex,
      imageSpanIndex + 1,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannable
  }

  private fun buildImageSpan(resources: Resources, @DrawableRes drawableId: Int): ImageSpan? {
    val downloadIcon: Drawable? =
      ContextCompat.getDrawable(binding.root.context, drawableId)
    downloadIcon ?: return null
    downloadIcon.setBounds(
      0,
      0,
      resources.getDimensionPixelSize(R.dimen.icon_height_width_1),
      resources.getDimensionPixelSize(R.dimen.icon_height_width_1)
    )
    val verticalAlignment = if (hasQ()) {
      ImageSpan.ALIGN_CENTER
    } else {
      ImageSpan.ALIGN_BASELINE
    }
    return ImageSpan(downloadIcon, verticalAlignment)
  }

  private fun getRetryButtonLabelForAdapterType(
    resources: Resources,
    type: ContentAdapter.Type
  ) =
    when (type) {
      ContentAdapter.Type.ContentWithSearch,
      ContentAdapter.Type.Content,
      ContentAdapter.Type.ContentWithFilters ->
        resources.getString(R.string.button_retry)
      ContentAdapter.Type.ContentBookmarked,
      ContentAdapter.Type.ContentCompleted,
      ContentAdapter.Type.ContentInProgress,
      ContentAdapter.Type.ContentDownloaded ->
        resources.getString(R.string.button_explore_tutorials)
    }

  private fun getRetryActionIconResource(
    type: ContentAdapter.Type
  ) =
    when (type) {
      ContentAdapter.Type.ContentWithSearch,
      ContentAdapter.Type.Content,
      ContentAdapter.Type.ContentWithFilters -> 0
      ContentAdapter.Type.ContentBookmarked,
      ContentAdapter.Type.ContentCompleted,
      ContentAdapter.Type.ContentInProgress,
      ContentAdapter.Type.ContentDownloaded ->
        R.drawable.ic_material_button_icon_arrow_right_green_contained
    }

  private fun getEmptyDrawable(
    type: ContentAdapter.Type
  ): Int? =
    when (type) {
      ContentAdapter.Type.ContentBookmarked ->
        R.drawable.ic_bookmarks
      ContentAdapter.Type.ContentCompleted ->
        R.drawable.ic_completed
      ContentAdapter.Type.ContentInProgress ->
        R.drawable.ic_in_progress
      ContentAdapter.Type.ContentDownloaded ->
        R.drawable.ic_suitcaseempty
      else -> null
    }

  companion object {
    /**
     * Factory function to create [ItemErrorViewHolder]
     */
    fun create(
      parent: ViewGroup, layoutId: Int,
      retryCallback: () -> Unit,
      emptyCallback: (() -> Unit)?
    ): ItemErrorViewHolder =
      ItemErrorViewHolder(
        DataBindingUtil.inflate(
          LayoutInflater.from(parent.context),
          layoutId, parent, false
        ), retryCallback, emptyCallback
      )
  }
}
