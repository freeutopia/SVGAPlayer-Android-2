package com.utopia.svga.drawer

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.utopia.svga.compose.SVGADynamicEntity
import com.utopia.svga.compose.SVGAVideoEntity

internal class SVGADrawable(
  private val imageView: ImageView,
  private val videoItem: SVGAVideoEntity,
  private val dynamicItem: SVGADynamicEntity?
) : Drawable() {

  private val drawer = SVGACanvasDrawer(videoItem, dynamicItem)

  @Volatile
  var currentFrame = 0
    internal set(value) {
      if (field == value) {
        return
      }

      field = value
      invalidateSelf()
    }

  override fun draw(canvas: Canvas) {
    if (isVisible) {
      drawer.drawFrame(canvas, currentFrame, imageView.scaleType)
    }
  }

  override fun setAlpha(alpha: Int) {}

  override fun getOpacity(): Int {
    return PixelFormat.TRANSPARENT
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {}

}