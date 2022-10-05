package com.tantan.library.svga.data.fetcher

import com.tantan.library.svga.utils.ByteBufferUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer

class ByteBufferFileResourceFetcher(filePath: String) :
  FileResourceFetcher<ByteBuffer>(filePath) {

  override fun loadResource(path: String): ByteBuffer? =
    File(path).takeIf { it.exists() && it.isFile }
      ?.let { ByteBufferUtil.fromStream(FileInputStream(it)) }

  @Throws(IOException::class)
  override fun close(data: ByteBuffer?) {
    data?.clear()
  }
}