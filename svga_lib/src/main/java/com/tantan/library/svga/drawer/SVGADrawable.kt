package com.tantan.library.svga.drawer

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.tantan.library.svga.compose.SVGADynamicEntity
import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.drawer.audio.SVGAAudioDrawer

internal class SVGADrawable(
  private val imageView: ImageView,
  private val videoItem: SVGAVideoEntity,
  private val dynamicItem: SVGADynamicEntity?
) : Drawable() {

  private val drawer = SVGACanvasDrawer(videoItem, dynamicItem)
  private val audioDrawer = SVGAAudioDrawer(videoItem)

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
    audioDrawer.playAudio(isVisible, currentFrame)
  }

  override fun setAlpha(alpha: Int) {}

  override fun getOpacity(): Int {
    return PixelFormat.TRANSPARENT
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {}

  internal fun resume() {
    audioDrawer.resume()
  }

  internal fun pause() {
    audioDrawer.pause()
  }
}