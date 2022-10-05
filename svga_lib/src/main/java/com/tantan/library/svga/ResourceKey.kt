package com.tantan.library.svga

import android.text.TextUtils
import android.util.Size
import com.tantan.library.svga.data.cache.CacheProvider
import com.tantan.library.svga.data.cache.Key
import com.tantan.library.svga.utils.Util
import java.io.File

class ResourceKey private constructor(builder: KeyBuilder) : Key {
  var path: String = builder.path //共用：url 或 assetPath
  private val error: String? = builder.error//加载失败的时候，触发加载兜底动画
  internal val isCacheable: Boolean = builder.isCacheable//是否使用缓存
  private val uuid = Util.strToMd5(path)
  override fun get(): String = path

  override fun cacheKey(): String = uuid

  fun transToErrorKey(): ResourceKey? {
    if (!TextUtils.isEmpty(error)) {
      path = error!!
      return this
    }
    return null
  }

  class KeyBuilder {
    internal lateinit var path: String
    internal var error: String? = null
    internal var isCacheable: Boolean = false

    fun path(path: String): KeyBuilder {
      this.path = path
      return this
    }

    fun error(error: String): KeyBuilder {
      this.error = error
      return this
    }


    fun build(): ResourceKey = ResourceKey(this)
  }
}