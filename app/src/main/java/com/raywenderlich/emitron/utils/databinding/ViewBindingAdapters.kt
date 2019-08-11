package com.raywenderlich.emitron.utils.databinding

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * Custom binding adapter for Views
 */
object ViewBindingAdapters {

  /**
   * Glide data binding adapter
   * Custom binding adapter to fetch and load image in an [AppCompatImageView] using [Glide]
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

      Glide.with(imageView).load(url)
        .placeholder(placeholder)
        .transition(
          DrawableTransitionOptions().crossFade()
        ).into(imageView)
    }
  }
}
