package com.utopia.svga.data.cache

import android.content.res.AssetManager
import com.utopia.svga.data.cache.disk.DiskCache
import com.utopia.svga.data.cache.memory.ResourceLruCache
import com.utopia.svga.data.cache.active.ActiveResource

internal object CacheProvider {
  var assetsProvider: AssetManager? = null
  var diskCache: DiskCache? = null
  var memoryLruCache: ResourceLruCache? = null
  val activeResource = ActiveResource()
}