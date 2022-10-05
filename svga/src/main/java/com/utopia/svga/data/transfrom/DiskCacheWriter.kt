package com.utopia.svga.data.transfrom

import com.utopia.svga.data.cache.disk.DiskCache
import java.io.File
import java.io.IOException

class DiskCacheWriter<DataType>(
  private val encoder: Encoder<DataType>,
  private val data: DataType?
) : DiskCache.Writer {
  override fun write(file: File): Boolean {
    try {
      data?.let {
        encoder.encode(it, file)
        return true
      }
      return false
    } catch (ex: IOException) {
      return false
    }
  }
}