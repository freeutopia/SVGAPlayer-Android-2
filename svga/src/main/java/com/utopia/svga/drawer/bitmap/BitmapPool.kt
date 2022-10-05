package com.utopia.svga.drawer.bitmap

import android.graphics.Bitmap
import com.utopia.svga.utils.ProxyRunnable
import com.utopia.svga.utils.SVGAExecutors
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

class BitmapPool private constructor() {
  private val keyPool = ConcurrentHashMap<String, Key>()
  private val bitmapReusePool = ConcurrentHashMap<Key, SoftReference<Bitmap>>()
  private val refQueue = ReferenceQueue<Bitmap>()

  init {
    SVGAExecutors.io.execute(ProxyRunnable("ReferenceQueue") {
      var k: Reference<*>? = null
      while (refQueue.remove()?.also { k = it } != null) {
        try {
          (k?.get() as? Bitmap)?.recycle()
        } catch (ignore: InterruptedException) {
          ignore.printStackTrace()
        }
      }
    })
  }

  @Synchronized
  fun getReuseBitmap(allocMemory: Int): Bitmap? {
    var inBitmap: Bitmap? = null
    val iterator = keyPool.iterator()
    while (iterator.hasNext()) {
      val next = iterator.next()
      if (next.value.size >= allocMemory && next.value.size <= allocMemory shl 3) {
        iterator.remove()
        inBitmap = bitmapReusePool.remove(next.value)?.get()?.takeIf { !it.isRecycled }
      }

      if (inBitmap != null) {
        return inBitmap
      }
    }
    return inBitmap
  }

  @Synchronized
  fun put(uuid: String, value: Bitmap?) {
    var key = keyPool[uuid]
    if (key == null && value?.isMutable == true) {
      key = Key(value.allocationByteCount)
      keyPool[uuid] = key
      bitmapReusePool[key] = SoftReference(value, refQueue)
    } else {
      value?.recycle()
    }
  }

  @Synchronized
  operator fun get(uuid: String): Bitmap? {
    keyPool.remove(uuid)?.let { key ->
      return bitmapReusePool.remove(key)?.get()
    }
    return null
  }

  companion object {
    private val INSTANCE = BitmapPool()
    fun get(): BitmapPool {
      return INSTANCE
    }
  }

  private class Key(val size: Int) : Comparable<Key> {
    override fun compareTo(other: Key): Int {
      return size - other.size
    }
  }
}