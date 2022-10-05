package com.utopia.svga.data.cache.disk

import java.io.File

interface DiskCache {

  interface Factory {
    fun build(): DiskCache?
  }

  interface Writer {
    fun write(file: File): Boolean
  }

  operator fun get(key: String): File?

  fun put(key: String, writer: Writer): File?

  fun delete(key: String)

  fun clear()

  fun size(): Long?
}