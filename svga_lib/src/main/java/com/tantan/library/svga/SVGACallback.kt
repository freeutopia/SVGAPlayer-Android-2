package com.tantan.library.svga

import com.tantan.library.svga.exception.SVGAException

/**
 * 动画生命周期回调
 */
abstract class AnimListener {
  open fun onStart() {}
  open fun onPause() {}
  open fun onFinished() {}
  open fun onRepeat() {}
  open fun onStep(frame: Int) {}
}

/**
 * 区域点击事件
 */
interface SVGAClickAreaListener {
  fun onClick(clickKey: String)
}

/**
 * 音频回调
 */
internal interface SoundCallBack {

  // 音量发生变化
  fun onVolumeChange(value: Float)

  // 音频加载完成
  fun onComplete(soundId: Int)
}

/**
 * 文件加载全局监听
 */
interface LoadResourceListener {
  fun onSuccess(path: String)
  fun onFailed(path: String, e: SVGAException)
}