package com.tantan.library.svga.data.fetcher

import java.io.IOException

abstract class FileResourceFetcher<T>(
  private val filePath: String
) : DataFetcher<T> {
  private var data: T? = null

  @Throws(IOException::class)
  override fun loadData(): T? {
    data = loadResource(filePath)
    return data
  }

  override fun recycle() {
    try {
      close(data)
    } catch (ignore: IOException) {
      // Ignored.
    }
  }


  @Throws(IOException::class)
  protected abstract fun loadResource(path: String): T?

  @Throws(IOException::class)
  protected abstract fun close(data: T?)
}