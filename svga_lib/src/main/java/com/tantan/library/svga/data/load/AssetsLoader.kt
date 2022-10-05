package com.tantan.library.svga.data.load

import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.compose.proto.MovieEntity
import com.tantan.library.svga.data.cache.CachedResource
import com.tantan.library.svga.data.cache.Resource
import com.tantan.library.svga.data.fetcher.DataFetcher
import com.tantan.library.svga.data.fetcher.StreamAssetResourceFetcher
import com.tantan.library.svga.tracker.CacheTrackerManager
import com.tantan.library.svga.tracker.CacheType
import com.tantan.library.svga.utils.ByteBufferUtil
import java.io.File
import java.io.InputStream
import java.lang.StringBuilder

class AssetsLoader(
  private val fetcher: DataFetcher<InputStream>
) : DataLoader<InputStream, Resource<*>>() {

  companion object {
    fun builder(key: ResourceKey, dir: String): AssetsLoader {
      return AssetsLoader(
        StreamAssetResourceFetcher(
          CacheProvider.assetsProvider,
          StringBuilder(dir).append(File.separator).append(key.get()).toString()
        )
      )
    }
  }

  override fun findData(key: ResourceKey) = fetcher.loadData()

  override fun processData(key: ResourceKey, data: Resource<*>?) {
    data?.takeIf { key.isCacheable }?.let {
      CacheProvider.memoryLruCache?.put(key.cacheKey(), it)
      CacheTrackerManager.onCachePut(CacheType.MEMORY, key) {
        Pair(it.size, CacheProvider.memoryLruCache?.getCurrentMemory() ?: 0)
      }
    }
    fetcher.recycle()
  }

  override fun transform(key: ResourceKey, source: InputStream?): Resource<*>? {
    return ByteBufferUtil.toBytes(source)?.run { ByteBufferUtil.inflate(this) }
      ?.run { CachedResource(SVGAVideoEntity(key, MovieEntity.ADAPTER.decode(this))) }
  }

  override fun getCacheType() = CacheType.ASSETS
}