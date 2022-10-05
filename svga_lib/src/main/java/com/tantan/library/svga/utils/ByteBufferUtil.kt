package com.tantan.library.svga.utils

import com.tantan.library.svga.exception.SVGAException
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.zip.Inflater

object ByteBufferUtil {

  private const val BUFFER_SIZE = 4096 // 2 Kb

  @Throws(IOException::class)
  fun toFile(buffer: ByteBuffer, file: File?) {
    rewind(buffer)
    var raf: RandomAccessFile? = null
    var channel: FileChannel? = null
    try {
      raf = RandomAccessFile(file, "rw")
      channel = raf.channel?.apply {
        write(buffer)
        force(false)
      }
    } finally {
      Util.close(channel)
      Util.close(raf)
    }
  }


  @Throws(IOException::class)
  fun toBytes(inputStream: InputStream?) =
    if (inputStream == null) null else toBytes(fromStream(inputStream))

  fun toBytes(byteBuffer: ByteBuffer?): ByteArray? {
    if (byteBuffer == null) {
      return null
    }
    val result: ByteArray
    val safeArray = getSafeArray(byteBuffer)
    if (safeArray != null && safeArray.offset == 0 && safeArray.limit == safeArray.data.size) {
      result = byteBuffer.array()
    } else {
      val toCopy = byteBuffer.asReadOnlyBuffer()
      result = ByteArray(toCopy.limit())
      rewind(toCopy)
      toCopy[result]
    }
    return result
  }

  @Throws(SVGAException::class)
  fun inflate(data: ByteArray): ByteArray? {
    val inflater = Inflater().apply {
      setInput(data, 0, data.size)
    }
    val inflatedBytes = ByteArray(BUFFER_SIZE)
    val outputStream = ByteArrayOutputStream()
    var count: Int
    try {
      while (true) {
        count = inflater.inflate(inflatedBytes, 0, BUFFER_SIZE)
        if (count <= 0) {
          break
        }
        outputStream.write(inflatedBytes, 0, count)
      }
      outputStream.flush()
      return outputStream.toByteArray()
    } catch (e: Throwable) {
      throw SVGAException("svga文件格式错误！", e)
    } finally {
      Util.close(outputStream)
      inflater.end()
    }
  }


  @Throws(IOException::class)
  fun fromStream(inputStream: InputStream?): ByteBuffer? {
    if (inputStream == null) {
      return null
    }

    val outStream = ByteArrayOutputStream(BUFFER_SIZE)
    val buffer = ByteArray(BUFFER_SIZE)
    var len: Int
    while (inputStream.read(buffer).also { len = it } >= 0) {
      outStream.write(buffer, 0, len)
    }

    try {
      outStream.flush()
      val bytes = outStream.toByteArray()
      return rewind(ByteBuffer.allocateDirect(bytes.size).put(bytes))
    } finally {
      Util.close(inputStream)
      Util.close(outStream)
    }
  }

  private fun rewind(buffer: ByteBuffer) = buffer.position(0) as ByteBuffer

  private fun getSafeArray(byteBuffer: ByteBuffer?): SafeArray? {
    return byteBuffer?.takeIf { !it.isReadOnly && it.hasArray() }?.run {
      SafeArray(array(), arrayOffset(), limit())
    }
  }

  internal class SafeArray(val data: ByteArray, val offset: Int, val limit: Int)
}