package com.utopia.svga.compose.entities

import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import com.utopia.svga.compose.proto.ShapeEntity
import com.utopia.svga.utils.FMatrix
import java.util.*

val sharedPath = Path()

class SVGAVideoShapeEntity(obj: ShapeEntity) {

  class Styles {

    var fill = 0x00000000
      internal set

    var stroke = 0x00000000
      internal set

    var strokeWidth = 0.0f
      internal set

    var lineCap = "butt"
      internal set

    var lineJoin = "miter"
      internal set

    var miterLimit = 0
      internal set

    var lineDash = FloatArray(0)
      internal set

  }

  private var type = ShapeEntity.ShapeType.SHAPE

  private val args = HashMap<String, Any>()

  var styles: Styles? = null
    private set

  var matrix: FMatrix? = null
    private set

  val isKeep: Boolean
    get() = type == ShapeEntity.ShapeType.KEEP

  var shapePath: Path? = null

  init {
    parseType(obj)
    parseArgs(obj)
    parseStyles(obj)
    parseTransform(obj)
  }

  private fun parseType(obj: ShapeEntity) {
    obj.type?.let { this.type = it }
  }

  private fun parseArgs(obj: ShapeEntity) {
    args.clear()

    obj.shape?.let {
      args.put("d", it.d ?: "")
    }

    obj.ellipse?.let {
      args["x"] = it.x ?: 0.0f
      args["y"] = it.y ?: 0.0f
      args["radiusX"] = it.radiusX ?: 0.0f
      args["radiusY"] = it.radiusY ?: 0.0f
    }

    obj.rect?.let {
      args["x"] = it.x ?: 0.0f
      args["y"] = it.y ?: 0.0f
      args["width"] = it.width ?: 0.0f
      args["height"] = it.height ?: 0.0f
      args["cornerRadius"] = it.cornerRadius ?: 0.0f
    }
  }

  // 检查色域范围是否是 [0f, 1f]，或者是 [0f, 255f]
  private fun checkValueRange(color: ShapeEntity.ShapeStyle.RGBAColor) =
    when {
      (color.r ?: 0f) + (color.g ?: 0f) + (color.b ?: 0f) <= 3f -> 255f
      else -> 1f
    }

  // 检查 alpha 范围是否是 [0f, 1f]，有可能是 [0f, 255f]
  private fun checkAlphaValueRange(color: ShapeEntity.ShapeStyle.RGBAColor) =
    when {
      color.a <= 1f -> 255f
      else -> 1f
    }

  private fun parseStyles(obj: ShapeEntity) {
    obj.styles?.let {
      val styles = Styles()
      it.fill?.let { fillColor ->
        val mulValue = checkValueRange(fillColor)
        val alphaRangeValue = checkAlphaValueRange(fillColor)
        styles.fill = Color.argb(
          ((fillColor.a ?: 0f) * alphaRangeValue).toInt(),
          ((fillColor.r ?: 0f) * mulValue).toInt(),
          ((fillColor.g ?: 0f) * mulValue).toInt(),
          ((fillColor.b ?: 0f) * mulValue).toInt()
        )
      }

      it.stroke?.let { strokeColor ->
        val mulValue = checkValueRange(strokeColor)
        val alphaRangeValue = checkAlphaValueRange(strokeColor)
        styles.stroke = Color.argb(
          ((strokeColor.a ?: 0f) * alphaRangeValue).toInt(),
          ((strokeColor.r ?: 0f) * mulValue).toInt(),
          ((strokeColor.g ?: 0f) * mulValue).toInt(),
          ((strokeColor.b ?: 0f) * mulValue).toInt()
        )
      }

      styles.strokeWidth = it.strokeWidth ?: 0.0f
      it.lineCap?.let { lineCap ->
        when (lineCap) {
          ShapeEntity.ShapeStyle.LineCap.LineCap_BUTT -> styles.lineCap =
            "butt"
          ShapeEntity.ShapeStyle.LineCap.LineCap_ROUND -> styles.lineCap =
            "round"
          ShapeEntity.ShapeStyle.LineCap.LineCap_SQUARE -> styles.lineCap =
            "square"
        }
      }

      it.lineJoin?.let { lineJoin ->
        when (lineJoin) {
          ShapeEntity.ShapeStyle.LineJoin.LineJoin_BEVEL -> styles.lineJoin =
            "bevel"
          ShapeEntity.ShapeStyle.LineJoin.LineJoin_MITER -> styles.lineJoin =
            "miter"
          ShapeEntity.ShapeStyle.LineJoin.LineJoin_ROUND -> styles.lineJoin =
            "round"
        }
      }

      styles.miterLimit = (it.miterLimit ?: 0.0f).toInt()
      styles.lineDash = FloatArray(3)
      it.lineDashI?.let { lineDash -> styles.lineDash[0] = lineDash }
      it.lineDashII?.let { lineDash -> styles.lineDash[1] = lineDash }
      it.lineDashIII?.let { lineDash -> styles.lineDash[2] = lineDash }
      this.styles = styles
    }
  }

  private fun parseTransform(obj: ShapeEntity) {
    obj.transform?.let {
      matrix = FMatrix().apply { transform(it) }
    }
  }

  fun buildPath() {
    if (this.shapePath != null) {
      return
    }

    sharedPath.reset()
    when (this.type) {
      ShapeEntity.ShapeType.SHAPE -> {
        (this.args["d"] as? String)?.let {
          SVGAPathEntity(it).buildPath(sharedPath)
        }
      }
      ShapeEntity.ShapeType.ELLIPSE -> {
        val xv = this.args["x"] as? Number ?: return
        val yv = this.args["y"] as? Number ?: return
        val rxv = this.args["radiusX"] as? Number ?: return
        val ryv = this.args["radiusY"] as? Number ?: return
        val x = xv.toFloat()
        val y = yv.toFloat()
        val rx = rxv.toFloat()
        val ry = ryv.toFloat()
        sharedPath.addOval(RectF(x - rx, y - ry, x + rx, y + ry), Path.Direction.CW)
      }
      ShapeEntity.ShapeType.RECT -> {
        val xv = this.args["x"] as? Number ?: return
        val yv = this.args["y"] as? Number ?: return
        val wv = this.args["width"] as? Number ?: return
        val hv = this.args["height"] as? Number ?: return
        val crv = this.args["cornerRadius"] as? Number ?: return
        val x = xv.toFloat()
        val y = yv.toFloat()
        val width = wv.toFloat()
        val height = hv.toFloat()
        val cornerRadius = crv.toFloat()
        sharedPath.addRoundRect(
          RectF(x, y, x + width, y + height),
          cornerRadius,
          cornerRadius,
          Path.Direction.CW
        )
      }
      else -> {
      }
    }

    this.shapePath = Path()
    this.shapePath?.set(sharedPath)
  }

}
