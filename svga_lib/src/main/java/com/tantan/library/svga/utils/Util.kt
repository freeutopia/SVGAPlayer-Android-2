package com.tantan.library.svga.utils

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.security.MessageDigest

object Util {
  fun strToMd5(str: String): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    messageDigest.update(str.toByteArray(charset("UTF-8")))
    val digest = messageDigest.digest()
    var sb = ""
    for (b in digest) {
      sb += String.format("%02x", b)
    }
    return sb
  }

  fun close(io: Closeable?) {
    try {
      io?.close()
    } catch (ignore: IOException) {

    }
  }

  fun deleteAll(f: File) {
    if (f.isDirectory) {
      f.listFiles()?.map {
        deleteAll(it)
      }
    }
    f.delete();// 删除空目录或文件
  }
}