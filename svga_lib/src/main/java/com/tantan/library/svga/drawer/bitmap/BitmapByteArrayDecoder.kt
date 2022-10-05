package com.tantan.library.svga.drawer.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory

internal object BitmapByteArrayDecoder : BitmapDecoder<ByteArray>() {
  override fun onDecode(data: ByteArray, ops: BitmapFactory.Options): Bitmap? =
    BitmapFactory.decodeByteArray(data, 0, data.count(), ops)
}