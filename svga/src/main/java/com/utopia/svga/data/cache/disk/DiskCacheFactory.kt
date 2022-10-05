package com.utopia.svga.data.cache.disk

import java.io.File

open class DiskCacheFactory(
  private val cacheDirectoryGetter: CacheDirectoryGetter,
  private val diskCacheSize: Long
) : DiskCache.Factory {

  interface CacheDirectoryGetter {
    val cacheDirectory: File?
  }

  override fun build(): DiskCache? =
    cacheDirectoryGetter.cacheDirectory?.takeIf { it.isDirectory || it.mkdirs() }?.let {
      DiskLruCacheWrapper.create(it, diskCacheSize)
    }
}