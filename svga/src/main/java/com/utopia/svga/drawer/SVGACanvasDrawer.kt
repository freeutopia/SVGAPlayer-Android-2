package com.utopia.svga.drawer

import android.graphics.*
import android.os.Build
import android.text.StaticLayout
import android.text.TextUtils
import android.widget.ImageView
import com.utopia.svga.compose.SVGADynamicEntity
import com.utopia.svga.compose.SVGAVideoEntity
import com.utopia.svga.utils.DrawerSprite
import com.utopia.svga.utils.FMatrix
import com.utopia.svga.utils.PathCache
import java.util.*
import kotlin.collections.HashMap

internal class SVGACanvasDrawer(
  videoItem: SVGAVideoEntity,
  private val dynamicItem: SVGADynamicEntity?
) : SGVADrawer(videoItem) {

  private val sharedValues = ShareValues()
  private val drawTextCache: HashMap<String, Bitmap> = hashMapOf()
  private val pathCache = PathCache()

  private var beginIndexList: Array<Boolean>? = null
  private var endIndexList: Array<Boolean>? = null

  override fun drawFrame(canvas: Canvas, frameIndex: Int, scaleType: ImageView.ScaleType) {
    super.drawFrame(canvas, frameIndex, scaleType)
    this.pathCache.onSizeChanged(canvas)
    val sprites = requestFrameSprites(frameIndex)
    if (sprites.isEmpty()) return

    val matteSprites = mutableMapOf<String, DrawerSprite>()
    var saveID = -1
    beginIndexList = null
    endIndexList = null

    sprites.forEachIndexed { index, sprite ->
      sprite.imageKey?.let {
        if (it.endsWith(".matte")) {
          matteSprites[it] = sprite
          return@forEachIndexed
        }
      }
      /// Is matte begin
      if (isMatteBegin(index, sprites)) {
        saveID = canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), null)
      }
      /// Normal matte
      drawSprite(sprite, canvas, frameIndex)

      /// Is matte end
      if (isMatteEnd(index, sprites)) {
        matteSprites[sprite.matteKey]?.let {
          drawSprite(
            it,
            this.sharedValues.shareMatteCanvas(canvas.width, canvas.height),
            frameIndex
          )
          canvas.drawBitmap(
            this.sharedValues.sharedMatteBitmap(),
            0f,
            0f,
            this.sharedValues.shareMattePaint()
          )
          if (saveID != -1) {
            canvas.restoreToCount(saveID)
          } else {
            canvas.restore()
          }
          // Continue
          return@forEachIndexed
        }
      }
    }
    releaseFrameSprites(sprites)
  }

  private fun isMatteBegin(spriteIndex: Int, sprites: List<DrawerSprite>): Boolean {
    if (beginIndexList == null) {
      val boolArray = Array(sprites.count()) { false }
      sprites.forEachIndexed { index, sprite ->
        sprite.imageKey?.let {
          /// Filter matte sprite
          if (it.endsWith(".matte")) {
            // Continue
            return@forEachIndexed
          }
        }
        sprite.matteKey?.takeIf { it.isNotEmpty() }?.let {
          sprites[index - 1].let { lastSprite ->
            if (lastSprite.matteKey.isNullOrEmpty()) {
              boolArray[index] = true
            } else {
              if (lastSprite.matteKey != sprite.matteKey) {
                boolArray[index] = true
              }
            }
          }
        }
      }
      beginIndexList = boolArray
    }
    return beginIndexList?.get(spriteIndex) ?: false
  }

  private fun isMatteEnd(spriteIndex: Int, sprites: List<DrawerSprite>): Boolean {
    if (endIndexList == null) {
      val boolArray = Array(sprites.count()) { false }
      sprites.forEachIndexed { index, svgaDrawerSprite ->
        svgaDrawerSprite.imageKey?.let {
          /// Filter matte sprite
          if (it.endsWith(".matte")) {
            // Continue
            return@forEachIndexed
          }
        }
        svgaDrawerSprite.matteKey?.let {
          if (it.isNotEmpty()) {
            // Last one
            if (index == sprites.count() - 1) {
              boolArray[index] = true
            } else {
              sprites[index + 1].let { nextSprite ->
                if (nextSprite.matteKey.isNullOrEmpty()) {
                  boolArray[index] = true
                } else {
                  if (nextSprite.matteKey != svgaDrawerSprite.matteKey) {
                    boolArray[index] = true
                  }
                }
              }
            }
          }
        }
      }
      endIndexList = boolArray
    }
    return endIndexList?.takeIf { it.size > spriteIndex }?.get(spriteIndex) ?: false
  }

  private fun shareFrameMatrix(transform: FMatrix): FMatrix {
    val matrix = this.sharedValues.sharedMatrix()
    matrix.postScale(scaleInfo.scaleFx, scaleInfo.scaleFy)
    matrix.postTranslate(scaleInfo.tranFx, scaleInfo.tranFy)
    matrix.preConcat(transform)
    return matrix
  }

  private fun drawSprite(sprite: DrawerSprite, canvas: Canvas, frameIndex: Int) {
    drawImage(sprite, canvas)
    drawShape(sprite, canvas)
    drawDynamic(sprite, canvas, frameIndex)
  }

  private fun drawImage(sprite: DrawerSprite, canvas: Canvas) {
    val imageKey = sprite.imageKey ?: return
    val isHidden = dynamicItem?.dynamicHidden?.get(imageKey) ?: false
    if (isHidden) {
      return
    }
    val bitmapKey =
      if (imageKey.endsWith(".matte")) imageKey.substring(0, imageKey.length - 6) else imageKey
    val drawingBitmap = (dynamicItem?.dynamicImage?.get(bitmapKey) ?: videoItem.imageMap[bitmapKey])
      ?: return
    val frameMatrix = shareFrameMatrix(sprite.frame.matrix)
    val paint = this.sharedValues.sharedPaint()
    paint.isAntiAlias = true
    paint.isFilterBitmap = true
    paint.alpha = (sprite.frame.alpha * 255).toInt()

    if (sprite.frame.maskPath != null) {
      val maskPath = sprite.frame.maskPath ?: return
      canvas.save()
      val path = this.sharedValues.sharedPath()
      maskPath.buildPath(path)
      path.transform(frameMatrix)
      canvas.clipPath(path)
      frameMatrix.preScale(
        sprite.frame.layout.width / drawingBitmap.width,
        sprite.frame.layout.height / drawingBitmap.height
      )
      if (!drawingBitmap.isRecycled) {
        canvas.drawBitmap(drawingBitmap, frameMatrix, paint)
      }
      canvas.restore()
    } else {
      frameMatrix.preScale(
        sprite.frame.layout.width / drawingBitmap.width,
        sprite.frame.layout.height / drawingBitmap.height
      )
      if (!drawingBitmap.isRecycled) {
        canvas.drawBitmap(drawingBitmap, frameMatrix, paint)
      }
    }
    dynamicItem?.dynamicIClickArea?.let {
      it[imageKey]?.let { listener ->
        val matrixArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        frameMatrix.getValues(matrixArray)
        listener.onResponseArea(
          imageKey,
          matrixArray[2].toInt(),
          matrixArray[5].toInt(),
          (drawingBitmap.width * matrixArray[0] + matrixArray[2]).toInt(),
          (drawingBitmap.height * matrixArray[4] + matrixArray[5]).toInt()
        )
      }
    }
    drawTextOnBitmap(canvas, drawingBitmap, sprite, frameMatrix)
  }

  private fun drawTextOnBitmap(
    canvas: Canvas,
    drawingBitmap: Bitmap,
    sprite: DrawerSprite,
    frameMatrix: Matrix
  ) {
    if (dynamicItem == null) {
      return
    }

    if (dynamicItem.isTextDirty) {
      this.drawTextCache.clear()
      dynamicItem.isTextDirty = false
    }
    val imageKey = sprite.imageKey ?: return
    var textBitmap: Bitmap? = null

    dynamicItem.dynamicText[imageKey]?.let { drawingText ->
      dynamicItem.dynamicTextPaint[imageKey]?.let { drawingTextPaint ->
        drawTextCache[imageKey]?.let {
          textBitmap = it
        } ?: kotlin.run {
          textBitmap =
            Bitmap.createBitmap(drawingBitmap.width, drawingBitmap.height, Bitmap.Config.ARGB_8888)
          val drawRect = Rect(0, 0, drawingBitmap.width, drawingBitmap.height)

          drawingTextPaint.isAntiAlias = true
          val fontMetrics = drawingTextPaint.fontMetrics
          val top = fontMetrics.top
          val bottom = fontMetrics.bottom

          textBitmap?.let {
            val baseLineY = drawRect.centerY() - top / 2 - bottom / 2
            Canvas(it).drawText(
              drawingText,
              drawRect.centerX().toFloat(),
              baseLineY,
              drawingTextPaint
            )
          }

          drawTextCache.put(imageKey, textBitmap as Bitmap)
        }
      }
    }

    dynamicItem.dynamicBoringLayoutText[imageKey]?.let {
      drawTextCache[imageKey]?.let {
        textBitmap = it
      } ?: kotlin.run {
        it.paint.isAntiAlias = true

        textBitmap =
          Bitmap.createBitmap(drawingBitmap.width, drawingBitmap.height, Bitmap.Config.ARGB_8888)

        textBitmap?.run {
          val textCanvas = Canvas(this)
          textCanvas.translate(0f, ((drawingBitmap.height - it.height) / 2).toFloat())
          it.draw(textCanvas)
        }

        drawTextCache.put(imageKey, textBitmap as Bitmap)
      }
    }

    dynamicItem.dynamicStaticLayoutText.get(imageKey)?.let {
      drawTextCache[imageKey]?.let {
        textBitmap = it
      } ?: kotlin.run {
        it.paint.isAntiAlias = true
        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          val lineMax = try {
            val field = StaticLayout::class.java.getDeclaredField("mMaximumVisibleLineCount")
            field.isAccessible = true
            field.getInt(it)
          } catch (e: Exception) {
            Int.MAX_VALUE
          }
          StaticLayout.Builder
            .obtain(it.text, 0, it.text.length, it.paint, drawingBitmap.width)
            .setAlignment(it.alignment)
            .setMaxLines(lineMax)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        } else {
          StaticLayout(
            it.text,
            0,
            it.text.length,
            it.paint,
            drawingBitmap.width,
            it.alignment,
            it.spacingMultiplier,
            it.spacingAdd,
            false
          )
        }
        textBitmap =
          Bitmap.createBitmap(drawingBitmap.width, drawingBitmap.height, Bitmap.Config.ARGB_8888)

        textBitmap?.let {
          val textCanvas = Canvas(it)
          textCanvas.translate(0f, ((drawingBitmap.height - layout.height) / 2).toFloat())
          layout.draw(textCanvas)
        }

        drawTextCache.put(imageKey, textBitmap as Bitmap)
      }
    }

    textBitmap?.let {
      val paint = this.sharedValues.sharedPaint()
      paint.isAntiAlias = true
      paint.alpha = (sprite.frame.alpha * 255).toInt()
      if (sprite.frame.maskPath != null) {
        val maskPath = sprite.frame.maskPath ?: return@let
        canvas.save()
        canvas.concat(frameMatrix)
        canvas.clipRect(0, 0, drawingBitmap.width, drawingBitmap.height)
        val bitmapShader = BitmapShader(it, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        paint.shader = bitmapShader
        val path = this.sharedValues.sharedPath()
        maskPath.buildPath(path)
        canvas.drawPath(path, paint)
        canvas.restore()
      } else {
        paint.isFilterBitmap = true
        canvas.drawBitmap(it, frameMatrix, paint)
      }
    }
  }

  private fun drawShape(sprite: DrawerSprite, canvas: Canvas) {
    val frameMatrix = shareFrameMatrix(sprite.frame.matrix)
    sprite.frame.shapes.forEach { shape ->
      shape.buildPath()
      shape.shapePath?.let {
        val paint = this.sharedValues.sharedPaint()
        paint.reset()
        paint.isAntiAlias = true
        paint.alpha = (sprite.frame.alpha * 255).toInt()
        val path = this.sharedValues.sharedPath()
        path.reset()
        this.pathCache.buildPath(shape)?.let {
          path.addPath(it)
        }
        val shapeMatrix = this.sharedValues.sharedMatrix2()
        shapeMatrix.reset()
        shape.matrix?.let {
          shapeMatrix.postConcat(it)
        }
        shapeMatrix.postConcat(frameMatrix)
        path.transform(shapeMatrix)
        shape.styles?.fill?.let {
          if (it != 0x00000000) {
            paint.style = Paint.Style.FILL
            paint.color = it
            val alpha = 255.coerceAtMost(0.coerceAtLeast((sprite.frame.alpha * 255).toInt()))
            if (alpha != 255) {
              paint.alpha = alpha
            }
            if (sprite.frame.maskPath !== null) canvas.save()
            sprite.frame.maskPath?.let { maskPath ->
              val path2 = this.sharedValues.sharedPath2()
              maskPath.buildPath(path2)
              path2.transform(frameMatrix)
              canvas.clipPath(path2)
            }
            canvas.drawPath(path, paint)
            if (sprite.frame.maskPath !== null) canvas.restore()
          }
        }
        shape.styles?.strokeWidth?.let {
          if (it > 0) {
            paint.alpha = (sprite.frame.alpha * 255).toInt()
            paint.style = Paint.Style.STROKE
            shape.styles?.stroke?.let { color ->
              paint.color = color
              val alpha = 255.coerceAtMost(0.coerceAtLeast((sprite.frame.alpha * 255).toInt()))
              if (alpha != 255) {
                paint.alpha = alpha
              }
            }
            val scale = matrixScale(shapeMatrix)
            shape.styles?.strokeWidth?.let { width ->
              paint.strokeWidth = width * scale
            }
            shape.styles?.lineCap?.let { cap ->
              when {
                cap.equals("butt", true) -> paint.strokeCap = Paint.Cap.BUTT
                cap.equals("round", true) -> paint.strokeCap = Paint.Cap.ROUND
                cap.equals("square", true) -> paint.strokeCap = Paint.Cap.SQUARE
              }
            }
            shape.styles?.lineJoin?.let { join ->
              when {
                join.equals("miter", true) -> paint.strokeJoin = Paint.Join.MITER
                join.equals("round", true) -> paint.strokeJoin = Paint.Join.ROUND
                join.equals("bevel", true) -> paint.strokeJoin = Paint.Join.BEVEL
              }
            }
            shape.styles?.miterLimit?.let {
              paint.strokeMiter = it.toFloat() * scale
            }
            shape.styles?.lineDash?.takeIf { dash -> dash.size == 3 && (dash[0] > 0 || dash[1] > 0) }
              ?.let { dash ->
                paint.pathEffect = DashPathEffect(
                  floatArrayOf(
                    (if (dash[0] < 1.0f) 1.0f else dash[0]) * scale,
                    (if (dash[1] < 0.1f) 0.1f else dash[1]) * scale
                  ), dash[2] * scale
                )
              }
            if (sprite.frame.maskPath !== null) canvas.save()
            sprite.frame.maskPath?.let { maskPath ->
              val path2 = this.sharedValues.sharedPath2()
              maskPath.buildPath(path2)
              path2.transform(frameMatrix)
              canvas.clipPath(path2)
            }
            canvas.drawPath(path, paint)
            if (sprite.frame.maskPath !== null) canvas.restore()
          }
        }
      }

    }
  }

  private val matrixScaleTempValues = FloatArray(16)

  private fun matrixScale(matrix: Matrix): Float {
    matrix.getValues(matrixScaleTempValues)
    if (matrixScaleTempValues[0] == 0f) {
      return 0f
    }
    var A = matrixScaleTempValues[0].toDouble()
    var B = matrixScaleTempValues[3].toDouble()
    var C = matrixScaleTempValues[1].toDouble()
    var D = matrixScaleTempValues[4].toDouble()
    if (A * D == B * C) return 0f
    var scaleX = Math.sqrt(A * A + B * B)
    A /= scaleX
    B /= scaleX
    var skew = A * C + B * D
    C -= A * skew
    D -= B * skew
    val scaleY = Math.sqrt(C * C + D * D)
    C /= scaleY
    D /= scaleY
    skew /= scaleY
    if (A * D < B * C) {
      scaleX = -scaleX
    }
    return if (scaleInfo.ratioX) Math.abs(scaleX.toFloat()) else Math.abs(scaleY.toFloat())
  }

  private fun drawDynamic(sprite: DrawerSprite, canvas: Canvas, frameIndex: Int) {
    val imageKey = sprite.imageKey ?: return
    dynamicItem?.dynamicDrawer?.get(imageKey)?.let {
      val frameMatrix = shareFrameMatrix(sprite.frame.matrix)
      canvas.save()
      canvas.concat(frameMatrix)
      it.invoke(canvas, frameIndex)
      canvas.restore()
    }
    dynamicItem?.dynamicDrawerSized?.get(imageKey)?.let {
      val frameMatrix = shareFrameMatrix(sprite.frame.matrix)
      canvas.save()
      canvas.concat(frameMatrix)
      it.invoke(
        canvas,
        frameIndex,
        sprite.frame.layout.width.toInt(),
        sprite.frame.layout.height.toInt()
      )
      canvas.restore()
    }
  }

  class ShareValues {

    private val sharedPaint = Paint()
    private val sharedPath = Path()
    private val sharedPath2 = Path()
    private val sharedMatrix = FMatrix()
    private val sharedMatrix2 = FMatrix()

    private val shareMattePaint = Paint()
    private var shareMatteCanvas: Canvas? = null
    private var sharedMatteBitmap: Bitmap? = null

    fun sharedPaint(): Paint {
      sharedPaint.reset()
      return sharedPaint
    }

    fun sharedPath(): Path {
      sharedPath.reset()
      return sharedPath
    }

    fun sharedPath2(): Path {
      sharedPath2.reset()
      return sharedPath2
    }

    fun sharedMatrix(): FMatrix {
      sharedMatrix.reset()
      return sharedMatrix
    }

    fun sharedMatrix2(): Matrix {
      sharedMatrix2.reset()
      return sharedMatrix2
    }

    fun shareMattePaint(): Paint {
      shareMattePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
      return shareMattePaint
    }

    fun sharedMatteBitmap(): Bitmap {
      return sharedMatteBitmap as Bitmap
    }

    fun shareMatteCanvas(width: Int, height: Int): Canvas {
      if (shareMatteCanvas == null) {
        sharedMatteBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
      }
      return Canvas(sharedMatteBitmap!!)
    }
  }
}
