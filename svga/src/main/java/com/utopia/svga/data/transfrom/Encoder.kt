package com.utopia.svga.data.transfrom

import java.io.File
import java.io.IOException

interface Encoder<T> {
  @Throws(IOException::class)
  fun encode(data: T, file: File)
}