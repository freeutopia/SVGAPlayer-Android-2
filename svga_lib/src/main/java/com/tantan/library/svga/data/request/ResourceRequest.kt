package com.tantan.library.svga.data.request

import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.data.cache.Resource
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.data.load.ActiveLoader
import com.tantan.library.svga.exception.SVGAException
import com.tantan.library.svga.tracker.CacheTrackerManager

interface RequestCallback {
  fun onResourceReady(key: ResourceKey, resource: Resource<*>)
  fun onLoadFailed(key: ResourceKey, e: SVGAException?)
}

class ResourceRequest(
  val key: ResourceKey,
  private val target: AbsTarget<Resource<SVGAVideoEntity>>
) : RequestCallback {
  private enum class Status {
    RUNNING,
    READY,
    COMPLETE,
    FAILED
  }

  private var resource: Resource<SVGAVideoEntity>? = null
  private var status: Status? = null

  @Synchronized
  fun begin() {
    when (status) {
      Status.RUNNING -> return
      Status.COMPLETE -> onResourceReady(key, resource!!)
      else -> {
        status = Status.READY
        onReady()
      }
    }
  }

  @Synchronized
  private fun onReady() {
    if (status == Status.READY) {
      status = Status.RUNNING

      val data = ActiveLoader.builder(key).callback(this).loadData(key)
      //如果返回结果为null,则尝试加载错误数据
      data ?: key.transToErrorKey()?.let { ActiveLoader.builder(key).callback(this).loadData(it) }
    }
  }

  override fun onResourceReady(key: ResourceKey, resource: Resource<*>) {
    resource.get().takeIf { it is SVGAVideoEntity }?.let {
      status = Status.COMPLETE
      target.onLoadSuccess(key, resource as Resource<SVGAVideoEntity>)
    } ?: onLoadFailed(key, SVGAException("数据加载错误，类型不匹配！"))
  }

  override fun onLoadFailed(key: ResourceKey, e: SVGAException?) {
    status = Status.FAILED
    e?.let { target.onLoadFailed(key, it) }
  }
}