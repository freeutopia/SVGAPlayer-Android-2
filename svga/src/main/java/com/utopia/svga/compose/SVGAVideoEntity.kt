package com.utopia.svga.compose

import DEFAULT_FPS
import DEFAULT_FRAMES
import android.graphics.Bitmap
import android.graphics.Rect
import com.utopia.svga.ResourceKey
import com.utopia.svga.compose.entities.SVGALayer
import com.utopia.svga.compose.proto.MovieEntity
import com.utopia.svga.compose.parser.ImageDataParser
import com.utopia.svga.compose.parser.LayerDataParser
import com.utopia.svga.drawer.bitmap.BitmapPool
import com.utopia.svga.utils.Util
import java.io.*
import kotlin.collections.HashMap

class SVGAVideoEntity(
  val key: ResourceKey,
  private val movieItem: MovieEntity
) {
  var videoSize = Rect(0, 0, 1, 1)
    private set

  var fps = DEFAULT_FPS
    private set

  var frames: Int = DEFAULT_FRAMES
    private set

  internal val spriteList = mutableListOf<SVGALayer>()
  val imageMap = HashMap<String, Bitmap>()

  private val layerDataParser = LayerDataParser()
  private lateinit var imageDataParser: ImageDataParser

  init {
    movieItem.params?.let { params ->
      videoSize = Rect(
        0, 0, params.viewBoxWidth?.toInt()
          ?: 1, params.viewBoxHeight?.toInt() ?: 1
      )
      fps = params.fps ?: DEFAULT_FPS
      frames = params.frames ?: DEFAULT_FRAMES
      imageDataParser = ImageDataParser(key, videoSize.width(), videoSize.height())
    }
  }

  fun resizeBitmap(width: Int, height: Int) {
    imageDataParser.resetSize(width, height)
  }

  @Synchronized
  fun load() {

    layerDataParser.takeIf { spriteList.isEmpty() }?.parser(movieItem) {
      spriteList.addAll(it)
    }

    imageDataParser.takeIf { imageMap.isEmpty() }?.parser(movieItem) {
      imageMap.putAll(it)
    }
  }

  @Synchronized
  fun clear() {
    imageMap.map { bitmap ->
      BitmapPool.get().put(key.cacheKey() + bitmap.key, bitmap.value)
    }.let {
      imageMap.clear()
    }
  }

  fun memorySize(): Long {
    var totalSize: Long = 0
    imageMap.forEach {
      totalSize += it.value.allocationByteCount
    }

    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    try {
      oos.writeObject(movieItem)
      oos.flush()
      totalSize += baos.size()
    } catch (ignore: Throwable) {
      ignore.printStackTrace()
    } finally {
      Util.close(baos)
      Util.close(oos)
    }
    return totalSize
  }

}

