package com.utopia.svga.data.transfrom

import com.utopia.svga.utils.ByteBufferUtil
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

class ByteBufferEncoder : Encoder<ByteBuffer> {

  @Throws(IOException::class)
  override fun encode(data: ByteBuffer, file: File) {
    ByteBufferUtil.toFile(data, file)
  }

}