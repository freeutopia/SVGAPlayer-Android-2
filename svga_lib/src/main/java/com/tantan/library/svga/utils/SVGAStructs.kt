package com.tantan.library.svga.utils

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Path
import com.tantan.library.svga.compose.entities.SVGAFrame
import com.tantan.library.svga.compose.entities.SVGAVideoShapeEntity
import com.tantan.library.svga.compose.proto.Layout
import com.tantan.library.svga.compose.proto.Transform

data class FPoint(val x: Float, val y: Float)

data class DrawerSprite(
  var matteKey: String? = null,
  var imageKey: String? = null,
  var frame: SVGAFrame
)

class PathCache {
  private var canvasWidth: Int = 0
  private var canvasHeight: Int = 0
  private val cache = HashMap<SVGAVideoShapeEntity, Path>()

  fun onSizeChanged(canvas: Canvas) {
    if (canvasWidth != canvas.width || canvasHeight != canvas.height) {
      cache.clear()
    }
    canvasWidth = canvas.width
    canvasHeight = canvas.height
  }

  fun buildPath(shape: SVGAVideoShapeEntity): Path? {
    if (!cache.containsKey(shape)) {
      val path = Path()
      shape.shapePath?.let { path.set(it) }
      cache[shape] = path
    }
    return cache[shape]
  }

}

data class FRect(var x: Float, var y: Float, var width: Float, var height: Float) {
  internal fun transform(from: Layout? = null) {
    if (((from?.x ?: 0f) + (from?.y ?: 0f) + (from?.width ?: 0f) + (from?.height ?: 0f)) <= 0f) {
      x = 0f
      y = 0f
      width = 1f
      height = 1f
    } else {
      x = from?.x ?: 0f
      y = from?.y ?: 0f
      width = from?.width ?: 1f
      height = from?.height ?: 1f
    }
  }
}

class FMatrix : Matrix() {
  internal fun transform(it: Transform? = null) {
    setValues(
      floatArrayOf(
        it?.a ?: 1.0f,
        it?.c ?: 0.0f,
        it?.tx ?: 0.0f,
        it?.b ?: 0.0f,
        it?.d ?: 1.0f,
        it?.ty ?: 0.0f,
        0.0f,
        0.0f,
        1.0f
      )
    )
  }
}