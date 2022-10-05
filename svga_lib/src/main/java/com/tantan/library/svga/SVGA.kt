package com.tantan.library.svga

import MB
import android.app.Application
import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.data.cache.disk.DiskCacheFactory
import com.tantan.library.svga.data.cache.disk.DiskCacheFactory.CacheDirectoryGetter
import com.tantan.library.svga.data.cache.memory.ResourceLruCache
import com.tantan.library.svga.exception.GlobalExceptionMonitor
import com.tantan.library.svga.exception.SVGAException
import com.tantan.library.svga.tracker.CacheTrackerManager
import com.tantan.library.svga.utils.SVGASoundManager
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
    SVGASoundManager.init(File(config.diskCacheDir ?: app.filesDir, "audio"))
  }

}