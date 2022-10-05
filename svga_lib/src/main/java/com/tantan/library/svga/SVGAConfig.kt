package com.tantan.library.svga

import com.tantan.library.svga.tracker.CacheTrackerListener
import java.io.File

class SVGAConfig private constructor(builder: Builder) {
  val enableCacheTrace: Boolean
  val diskCacheDir: File?
  val diskCacheSize: Long
  val listener: LoadResourceListener?
  val trackerListener: CacheTrackerListener?

  class Builder {
    internal var enableCacheTrace = false
    internal var diskCacheDir: File? = null
    internal var diskCacheSize = 50 * MB
    internal var listener: LoadResourceListener? = null
    internal var trackerListener: CacheTrackerListener? = null

    fun setDiskCacheDir(dir: File): Builder {
      require(dir.isDirectory) { "cache file must be a directory" }
      diskCacheDir = dir
      return this
    }

    fun setDiskCacheSize(diskCacheSize: Long) {
      require(diskCacheSize >= MB) { "cache size must be > 1MB" }
      this.diskCacheSize = diskCacheSize
    }

    fun enableCacheTrace(enable: Boolean): Builder {
      enableCacheTrace = enable
      return this
    }

    fun setLoadResourceListener(listener: LoadResourceListener?): Builder {
      this.listener = listener
      return this
    }

    fun setTrackerListener(trackerListener: CacheTrackerListener?): Builder {
      this.trackerListener = trackerListener
      return this
    }

    fun build(): SVGAConfig {
      return SVGAConfig(this)
    }
  }

  companion object {
    private const val MB = 1024 * 1024L
  }

  init {
    enableCacheTrace = builder.enableCacheTrace
    diskCacheDir = builder.diskCacheDir
    diskCacheSize = builder.diskCacheSize
    listener = builder.listener
    trackerListener = builder.trackerListener
  }
}