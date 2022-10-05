package com.tantan.library.svga.data.cache.disk

import com.tantan.library.svga.data.cache.disk.disklrucache.DiskLruCache
import java.io.File
import java.io.IOException

class DiskLruCacheWrapper(private val directory: File?, private val maxSize: Long) : DiskCache {
  private var diskLruCache: DiskLruCache? = null

  @get:Throws(IOException::class)
  @get:Synchronized
  private val diskCache: DiskLruCache
    get() {
      if (diskLruCache == null) {
        diskLruCache = DiskLruCache.open(directory, APP_VERSION, VALUE_COUNT, maxSize)
      }
      return diskLruCache!!
    }

  override fun get(key: String): File? = diskCache[key]?.run { getFile(0) }

  override fun put(key: String, writer: DiskCache.Writer): File? {
    if (diskCache[key] != null) {
      return get(key)
    }

    diskCache.edit(key)?.apply {
      try {
        val file = getFile(0)
        if (writer.write(file)) {
          commit()
        }
        return file
      } finally {
        abortUnlessCommitted()
      }
    }
    return null
  }

  override fun delete(key: String) {
    diskCache.remove(key)
  }

  @Synchronized
  override fun clear() {
    try {
      diskCache.delete()
    } finally {
      diskLruCache = null
    }
  }

  override fun size(): Long? {
    return diskLruCache?.size()
  }

  companion object {
    private const val APP_VERSION = 1
    private const val VALUE_COUNT = 1

    fun create(directory: File?, maxSize: Long): DiskCache = DiskLruCacheWrapper(directory, maxSize)
  }

}