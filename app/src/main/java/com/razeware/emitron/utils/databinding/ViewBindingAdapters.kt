package com.razeware.emitron.utils.databinding

import android.graphics.drawable.Drawable
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * Custom binding adapter for Views
 */
object ViewBindingAdapters {

  /**
   * Glide data binding adapter
   * Custom binding adapter to fetch and load image in an [android.widget.ImageView] using [Glide]
   *
   * @param imageView ImageView to load image
   * @param url Url of image to be loaded
   * @param placeholder Placeholder drawable while image is being fetched
   */
  @BindingAdapter(value = ["imageUrl", "placeholder"], requireAll = false)
  @JvmStatic
  fun setImageUsingGlide(
    imageView: ImageView, url: String?, placeholder: Drawable?
  ) {

    if (!url.isNullOrBlank()) {
      // Outline clipping is helpful in drawing rounded corners
      imageView.clipToOutline = true

      Glide.with(imageView)
        .load(url)
        .placeholder(placeholder)
        .centerInside()
        .transition(
          DrawableTransitionOptions().crossFade()
        ).into(imageView)
    }
  }

  private fun View.doOnApplyWindowInsets(f: (View, WindowInsets, InitialPadding) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    setOnApplyWindowInsetsListener { v, insets ->
      f(v, insets, initialPadding)
      // Always return the insets, so that children can also use them
      insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
  }

  /**
   * Data class to save initial view padding
   */
  data class InitialPadding(
    /**
     * Left
     */
    val left: Int,
    /**
     * Top
     */
    val top: Int,
    /**
     * Right
     */
    val right: Int,
    /**
     * Bottom
     */
    val bottom: Int
  )

  private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
  )

  /**
   * Binding adapter to apply inset padding
   *
   * @param view View to apply inset padding
   * @param applyBottomInset true if bottom inset should be applied else false
   */
  @JvmStatic
  @BindingAdapter("paddingBottomSystemWindowInsets")
  fun applySystemWindowBottomInset(view: View, applyBottomInset: Boolean) {
    view.doOnApplyWindowInsets { insetView, insets, padding ->
      val bottom = if (applyBottomInset) insets.systemWindowInsetBottom else 0
      insetView.updatePadding(bottom = padding.bottom + bottom)
    }
  }

  private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
      // We're already attached, just request as normal
      requestApplyInsets()
    } else {
      // We're not attached to the hierarchy, add a listener to
      // request when we are
      addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
          v.removeOnAttachStateChangeListener(this)
          v.requestApplyInsets()
        }

        override fun onViewDetachedFromWindow(v: View) = Unit
      })
    }
  }
}
