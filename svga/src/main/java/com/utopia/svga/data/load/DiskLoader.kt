package com.utopia.svga.data.load

import com.utopia.svga.data.cache.CacheProvider
import com.utopia.svga.ResourceKey
import com.utopia.svga.compose.SVGAVideoEntity
import com.utopia.svga.compose.proto.MovieEntity
import com.utopia.svga.data.cache.CachedResource
import com.utopia.svga.data.cache.Resource
import com.utopia.svga.data.fetcher.ByteBufferFileResourceFetcher
import com.utopia.svga.data.fetcher.DataFetcher
import com.utopia.svga.tracker.CacheTrackerManager
import com.utopia.svga.tracker.CacheType
import com.utopia.svga.utils.ByteBufferUtil
import java.nio.ByteBuffer

class DiskLoader(
  private val fetcher: DataFetcher<ByteBuffer>,
  parent: DataLoader<*, ByteBuffer>?
) : DataLoader<ByteBuffer, Resource<*>>(parent) {

  companion object {
    fun builder(key: ResourceKey): DiskLoader {
      val networkLoader = NetworkLoader.builder(key)
      val path = CacheProvider.diskCache?.get(key.cacheKey())?.absolutePath
      return DiskLoader(ByteBufferFileResourceFetcher(path ?: ""), networkLoader)
    }
  }

  override fun findData(key: ResourceKey): ByteBuffer? {
    return fetcher.loadData()
  }

  override fun processData(key: ResourceKey, data: Resource<*>?) {
    data?.takeIf { key.isCacheable }?.let {
      CacheProvider.memoryLruCache?.put(key.cacheKey(), it)
      CacheTrackerManager.onCachePut(
        CacheType.MEMORY,
        key
      ) {
        Pair(it.size, CacheProvider.memoryLruCache?.getCurrentMemory() ?: 0)
      }
    }
    fetcher.recycle()
  }

  override fun transform(key: ResourceKey, source: ByteBuffer?): Resource<*>? {
    return ByteBufferUtil.toBytes(source)?.run { ByteBufferUtil.inflate(this) }
      ?.run {
        CachedResource(
          SVGAVideoEntity(
            key,
            MovieEntity.ADAPTER.decode(this)
          )
        )
      }
  }

  override fun getCacheType() = CacheType.DISK
}