package com.razeware.emitron.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.razeware.emitron.R
import com.razeware.emitron.utils.extensions.toPx


internal abstract class SwipeActionCallback(
  @DrawableRes
  val background: Int,
  @StringRes
  val buttonText: Int,
  val onSwipe: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

  companion object {

    fun build(
      @DrawableRes
      background: Int,
      @StringRes
      buttonText: Int,
      onSwipe: (Int) -> Unit
    ): SwipeActionCallback {
      return object : SwipeActionCallback(background, buttonText, onSwipe) {}
    }
  }

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    return false
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    onSwipe(viewHolder.adapterPosition)
  }

  override fun onChildDraw(
    c: Canvas,
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {

    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    val background = ContextCompat.getDrawable(
      recyclerView.context, background
    )
    val itemView = viewHolder.itemView
    background?.setBounds(
      (itemView.right + dX).toInt(),
      itemView.top, itemView.right, itemView.bottom
    )
    background?.draw(c)

    // Drawing text on canvas is simpler :]
    drawDeleteText(viewHolder, recyclerView.context, c)
  }

  private fun drawDeleteText(
    viewHolder: RecyclerView.ViewHolder,
    context: Context,
    canvas: Canvas
  ) {
    val swipeText = context.getString(buttonText)
    val textPaint = createTextPaint(context)
    val textBounds = Rect()
    textPaint.getTextBounds(swipeText, 0, swipeText.length, textBounds)
    val textHeight = textBounds.height()
    val textWidth = textPaint.measureText(swipeText)
    val textY = viewHolder.itemView.top + (viewHolder.itemView.height / 2) + (textHeight / 2)
    canvas.drawText(
      swipeText,
      viewHolder.itemView.right - (textWidth + textPaint.textSize),
      textY.toFloat(),
      textPaint
    )
  }

  private fun createTextPaint(ctx: Context) = TextPaint().apply {
    isAntiAlias = true
    textSize = 14.toPx().toFloat()
    color = ContextCompat.getColor(ctx, R.color.textColorButtonOnError)
    typeface = ResourcesCompat.getFont(ctx, R.font.roboto_bold)
  }
}
