package com.tantan.library.svga.data.fetcher

import android.content.res.AssetManager
import java.io.Closeable
import java.io.IOException

abstract class AssetResourceFetcher<T>(
  private val assetManager: AssetManager?,
  val path: String
) : DataFetcher<T> {
  private var data: T? = null

  @Throws(IOException::class)
  override fun loadData(): T? {
    data = loadResource(assetManager, path)
    return data
  }

  override fun recycle() {
    try {
      close(data)
    } catch (e: IOException) {
      // Ignored.
    }
  }

  @Throws(IOException::class)
  protected abstract fun loadResource(assetManager: AssetManager?, path: String): T?

  @Throws(IOException::class)
  protected abstract fun close(data: T?)
}