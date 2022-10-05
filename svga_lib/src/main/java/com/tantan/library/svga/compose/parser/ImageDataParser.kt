package com.tantan.library.svga.compose.parser

import android.graphics.Bitmap
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.compose.proto.MovieEntity
import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.drawer.bitmap.BitmapByteArrayDecoder
import com.tantan.library.svga.drawer.bitmap.BitmapPool
import com.tantan.library.svga.exception.GlobalExceptionMonitor
import com.tantan.library.svga.exception.SVGAException
import com.tantan.library.svga.utils.Log

internal class ImageDataParser(
  private val key: ResourceKey,
  private val originWidth: Int,
  private val originHeight: Int
) : DataParser<MovieEntity, Map<String, Bitmap>>() {
  private var realWidth: Int? = null
  private var realHeight: Int? = null

  internal fun resetSize(width: Int, height: Int) {
    realWidth = width
    realHeight = height
  }

  override fun onParser(data: MovieEntity): Map<String, Bitmap> {
    val imageMap = mutableMapOf<String, Bitmap>()
    data.images?.entries?.forEach { entry ->
      val byteArray = entry.value.toByteArray()
      if (byteArray.count() < 4) {
        return@forEach
      }
      val fileTag = byteArray.slice(IntRange(0, 3))
      if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) {
        return@forEach
      }

      try {
        val bitmap = BitmapPool.get()[key.cacheKey() + entry.key]
        if (bitmap != null) {
          imageMap[entry.key] = bitmap
        } else {
          BitmapByteArrayDecoder.decodeBitmapFrom(
            byteArray, realWidth ?: originWidth, realHeight ?: originHeight
          )?.let {
            imageMap[entry.key] = it
          }
        }
      } catch (e: Throwable) {
        GlobalExceptionMonitor.get()
          ?.onFailed(key.get(), SVGAException("解析SVGA-Bitmap资源(${key.get()})失败！", e))
      }
    }
    return imageMap
  }

}