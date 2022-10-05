package com.tantan.library.svga

import android.content.Context
import android.view.View
import com.tantan.library.svga.compose.SVGADynamicEntity
import com.tantan.library.svga.data.request.RequestCallback
import com.tantan.library.svga.data.request.ResourceRequest
import com.tantan.library.svga.data.request.ResourceTarget
import com.tantan.library.svga.data.request.ViewTarget
import com.tantan.library.svga.utils.Log
import java.util.concurrent.ConcurrentHashMap

object SVGALoader {
  @JvmStatic
  fun with(context: Context) = SVGARequestBuilder(context)
  internal val targetViews = ConcurrentHashMap<SVGAnimationView, String>()

  class SVGARequestBuilder(internal val context: Context) {
    private var keyBuilder: ResourceKey.KeyBuilder = ResourceKey.KeyBuilder()
    private var dynamicEntity: SVGADynamicEntity? = null
    private var autoPlay: Boolean = true
    private var audioEnable: Boolean = false
    private var repeatCount: Int? = null
    private var frameMode: SVGAnimationView.FrameMode? = null
    private var loadCallback: RequestCallback? = null
    private var animListener: AnimListener? = null

    fun from(source: String): SVGARequestBuilder {
      keyBuilder.path(source)
      return this
    }

    fun error(source: String): SVGARequestBuilder {
      keyBuilder.error(source)
      return this
    }

    fun isCacheable(canCache: Boolean): SVGARequestBuilder {
      keyBuilder.isCacheable = canCache
      return this
    }

    fun audioEnable(enable: Boolean): SVGARequestBuilder {
      this.audioEnable = enable
      return this
    }

    fun dynamic(entity: SVGADynamicEntity): SVGARequestBuilder {
      this.dynamicEntity = entity
      return this
    }

    fun autoPlay(auto: Boolean): SVGARequestBuilder {
      this.autoPlay = auto
      return this
    }

    fun repeatCount(count: Int): SVGARequestBuilder {
      this.repeatCount = count
      if (repeatCount == 0) {
        autoPlay = false
      }
      return this
    }

    fun frameMode(mode: SVGAnimationView.FrameMode): SVGARequestBuilder {
      this.frameMode = mode
      return this
    }

    fun loadCallback(callback: RequestCallback? = null): SVGARequestBuilder {
      this.loadCallback = callback
      return this
    }

    fun animListener(listener: AnimListener? = null): SVGARequestBuilder {
      this.animListener = listener
      return this
    }

    fun into(view: SVGAnimationView) {
      view.clearHistory()
      repeatCount?.let {
        view.mLoops = it
      }
      frameMode?.let {
        view.mFillMode = it
      }
      view.mAutoPlay = autoPlay
      view.audioEnable = audioEnable
      view.animListener = animListener
      keyBuilder.isCacheable = true
      startRequest(view, keyBuilder.build(), dynamicEntity)
    }

    internal fun startRequest(
      view: SVGAnimationView,
      key: ResourceKey,
      dynamic: SVGADynamicEntity?
    ) {
      if (!targetViews.containsValue(key.cacheKey())) {
        ViewTarget(loadCallback, dynamic).apply {
          request = ResourceRequest(key, this)
        }.begin()
      }
      targetViews[view] = key.cacheKey()
    }

    /**
     * 该队列任务不会立即执行，等待系统空闲时开启
     */
    fun downloadOnly() {
      ResourceTarget(callback = loadCallback).apply {
        request = ResourceRequest(keyBuilder.build(), this)
      }.begin()
    }

    /**
     * 批量下载
     */
    fun batchDownload(resources: List<String>?) {
      resources?.forEach { path ->
        ResourceTarget(loadCallback).apply {
          request = ResourceRequest(ResourceKey.KeyBuilder().path(path).build(), this)
        }.begin()
      }
    }
  }
}