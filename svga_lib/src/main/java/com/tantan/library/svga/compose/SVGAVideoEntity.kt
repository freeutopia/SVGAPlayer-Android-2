package com.tantan.library.svga.compose

import DEFAULT_FPS
import DEFAULT_FRAMES
import android.graphics.Bitmap
import android.graphics.Rect
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.utils.SVGASoundManager
import com.tantan.library.svga.compose.entities.SVGAAudio
import com.tantan.library.svga.compose.entities.SVGALayer
import com.tantan.library.svga.compose.proto.MovieEntity
import com.tantan.library.svga.compose.parser.AudioDataParser
import com.tantan.library.svga.compose.parser.ImageDataParser
import com.tantan.library.svga.compose.parser.LayerDataParser
import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.drawer.bitmap.BitmapPool
import com.tantan.library.svga.utils.Util
import java.io.*
import kotlin.collections.HashMap

class SVGAVideoEntity(val key: ResourceKey, private val movieItem: MovieEntity) {
  var videoSize = Rect(0, 0, 1, 1)
    private set

  var fps = DEFAULT_FPS
    private set

  var frames: Int = DEFAULT_FRAMES
    private set

  internal val spriteList = mutableListOf<SVGALayer>()
  internal val audioMap = HashMap<File, SVGAAudio>()
  val imageMap = HashMap<String, Bitmap>()

  private val layerDataParser = LayerDataParser()
  private lateinit var imageDataParser: ImageDataParser
  private var audioDataParser: AudioDataParser? = null

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
  fun checkAndParserAudio(audioEnable: Boolean, onReady: () -> Unit) {
    if (audioEnable) {
      audioDataParser = audioDataParser ?: AudioDataParser(this)
      audioDataParser?.takeIf { audioMap.isEmpty() }?.parser(movieItem) {
        audioMap.putAll(it)
      }
    }
    onReady()
  }

  @Synchronized
  fun load() {

    layerDataParser.takeIf { spriteList.isEmpty() }?.parser(movieItem) {
      spriteList.addAll(it)
    }

    imageDataParser.takeIf { imageMap.isEmpty() }?.parser(movieItem) {
      imageMap.putAll(it)
    }

    audioDataParser?.takeIf { audioMap.isEmpty() }?.parser(movieItem) {
      audioMap.putAll(it)
    }
  }

  @Synchronized
  fun clear() {
    audioMap.map { audio ->
      audio.value.playID?.let {
        SVGASoundManager.unload(it)
      }
    }.let {
      audioMap.clear()
    }

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

    audioMap.forEach {
      totalSize += it.key.length()
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

