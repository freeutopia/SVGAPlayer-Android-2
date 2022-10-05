package com.tantan.library.svga.drawer

import android.graphics.Canvas
import android.widget.ImageView
import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.utils.DrawerSprite
import com.tantan.library.svga.utils.Pools
import com.tantan.library.svga.utils.SVGAScaleInfo
import kotlin.math.max

internal open class SGVADrawer(val videoItem: SVGAVideoEntity) {

  val scaleInfo = SVGAScaleInfo()

  private val spritePool = Pools.ObjectPool<DrawerSprite>(max(1, videoItem.frames))

  internal fun requestFrameSprites(frameIndex: Int): List<DrawerSprite> {
    return videoItem.spriteList.mapNotNull {
      if (frameIndex in it.frames.indices) {
        it.imageKey?.let { key ->
          if (!key.endsWith(".matte") && it.frames[frameIndex].alpha <= 0f) {
            return@mapNotNull null
          }
          return@mapNotNull spritePool.acquire()?.apply {
            matteKey = it.matteKey
            imageKey = it.imageKey
            frame = it.frames[frameIndex]
          } ?: DrawerSprite(it.matteKey, it.imageKey, it.frames[frameIndex])
        }
      }
      return@mapNotNull null
    }
  }

  internal fun releaseFrameSprites(sprites: List<DrawerSprite>) {
    sprites.forEach { spritePool.release(it) }
  }

  open fun drawFrame(canvas: Canvas, frameIndex: Int, scaleType: ImageView.ScaleType) {
    scaleInfo.performScaleType(
      canvas.width.toFloat(),
      canvas.height.toFloat(),
      videoItem.videoSize.width().toFloat(),
      videoItem.videoSize.height().toFloat(),
      scaleType
    )
  }

}
