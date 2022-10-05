package com.tantan.library.svga.drawer.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.lang.Exception

internal abstract class BitmapDecoder<T> {

  fun decodeBitmapFrom(data: T, reqWidth: Int, reqHeight: Int): Bitmap? {
    return BitmapFactory.Options().run {
      // 如果期望的宽高是合法的, 则开启检测尺寸模式
      inJustDecodeBounds = (reqWidth > 0 && reqHeight > 0)
      val bitmap = onDecode(data, this)
      if (!inJustDecodeBounds) {
        return bitmap
      }
      //计算inSampleSize
      inSampleSize = calculate(this, reqWidth, reqHeight)
      inJustDecodeBounds = false
      inMutable = true
      inBitmap = BitmapPool.get().getReuseBitmap(outWidth * outHeight * 4 / inSampleSize)
      try {
        inBitmap?.eraseColor(Color.TRANSPARENT)
        onDecode(data, this)
      } catch (ignore: Exception) {
        inBitmap?.recycle()
        inBitmap = null
        onDecode(data, this)
      }
    }
  }

  abstract fun onDecode(data: T, ops: BitmapFactory.Options): Bitmap?

  private fun calculate(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (reqHeight <= 0 || reqWidth <= 0) {
      return inSampleSize
    }

    if (height > reqHeight || width > reqWidth) {
      val halfHeight: Int = height / 2
      val halfWidth: Int = width / 2
      while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
        inSampleSize *= 2
      }
    }

    return inSampleSize
  }
}