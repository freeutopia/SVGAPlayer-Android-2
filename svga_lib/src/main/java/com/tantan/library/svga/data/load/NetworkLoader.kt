package com.tantan.library.svga.data.load

import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.data.fetcher.DataFetcher
import com.tantan.library.svga.data.fetcher.NetworkResourceFetcher
import com.tantan.library.svga.data.transfrom.ByteBufferEncoder
import com.tantan.library.svga.data.transfrom.DiskCacheWriter
import com.tantan.library.svga.tracker.CacheTrackerManager
import com.tantan.library.svga.tracker.CacheType
import com.tantan.library.svga.utils.ByteBufferUtil
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer

class NetworkLoader(private val fetcher: DataFetcher<InputStream>) :
  DataLoader<InputStream, ByteBuffer>() {

  companion object {
    fun builder(key: ResourceKey): NetworkLoader =
      NetworkLoader(NetworkResourceFetcher(URL(key.get())))
  }

  override fun findData(key: ResourceKey) = fetcher.loadData()

  override fun processData(key: ResourceKey, data: ByteBuffer?) {
    CacheProvider.diskCache?.takeIf { data != null }?.run {
      put(key.cacheKey(), DiskCacheWriter(ByteBufferEncoder(), data))
    }?.let {
      CacheTrackerManager.onCachePut(CacheType.DISK, key) {
        Pair(it.length(), CacheProvider.diskCache?.size() ?: 0)
      }
    }
    fetcher.recycle()
  }

  override fun transform(key: ResourceKey, source: InputStream?) =
    ByteBufferUtil.fromStream(source)

  override fun getCacheType() = CacheType.NETWORK
}