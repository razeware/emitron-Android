package com.raywenderlich.emitron.utils

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
import com.raywenderlich.emitron.R
import com.raywenderlich.emitron.utils.extensions.toPx


internal abstract class SwipeActionCallback(
  @DrawableRes
  val background: Int,
  @StringRes
  val buttonText: Int
) :
  ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    return false
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

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
    val textPaint = TextPaint()
    textPaint.isAntiAlias = true
    textPaint.textSize = 14.toPx().toFloat()
    textPaint.color = ContextCompat.getColor(context, R.color.textColorButtonOnError)
    textPaint.typeface = ResourcesCompat.getFont(context, R.font.roboto_bold)

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
}
