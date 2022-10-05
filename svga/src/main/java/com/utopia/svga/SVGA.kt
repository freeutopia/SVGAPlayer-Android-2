package com.utopia.svga

import MB
import android.app.Application
import com.utopia.svga.data.cache.CacheProvider
import com.utopia.svga.data.cache.disk.DiskCacheFactory
import com.utopia.svga.data.cache.disk.DiskCacheFactory.CacheDirectoryGetter
import com.utopia.svga.data.cache.memory.ResourceLruCache
import com.utopia.svga.exception.GlobalExceptionMonitor
import com.utopia.svga.exception.SVGAException
import com.utopia.svga.tracker.CacheTrackerManager
import java.io.File

object SVGA {

  @JvmStatic
  fun init(app: Application, config: SVGAConfig) {
    initCacheProvider(app, config)
    initGlobalMonitor(config)
  }

  private fun initGlobalMonitor(config: SVGAConfig) {
    GlobalExceptionMonitor.register(config.listener ?: object : LoadResourceListener {
      override fun onSuccess(path: String) {
        //e("onSuccess:$path")
      }

      override fun onFailed(path: String, e: SVGAException) {
        e.printStackTrace()
      }
    })

    config.trackerListener?.takeIf { config.enableCacheTrace }?.let {
      CacheTrackerManager.register(it)
    }
  }

  private fun initCacheProvider(app: Application, config: SVGAConfig) {

    val directoryGetter = object : CacheDirectoryGetter {
      override val cacheDirectory: File
        get() = File(config.diskCacheDir ?: app.filesDir, "svga")
    }

    CacheProvider.assetsProvider = app.assets
    CacheProvider.diskCache = DiskCacheFactory(directoryGetter, config.diskCacheSize).build()
    CacheProvider.memoryLruCache = ResourceLruCache(8 * MB)
  }

}