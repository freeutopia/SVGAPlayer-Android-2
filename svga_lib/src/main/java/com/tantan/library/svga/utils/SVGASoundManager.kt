package com.tantan.library.svga.utils

import android.media.AudioAttributes
import android.media.SoundPool
import com.tantan.library.svga.SoundCallBack
import com.tantan.library.svga.compose.SVGAVideoEntity
import java.io.File

object SVGASoundManager {
  var cacheDir: File? = null
    set(value) {
      field = value?.apply {
        Util.deleteAll(this)
        takeIf { !exists() }?.mkdirs()
      }
    }
  var soundPool: SoundPool? = null
    private set

  private val soundCallBackMap: MutableMap<Int, SoundCallBack> = mutableMapOf()

  /**
   * 音量设置，范围在 [0, 1] 之间
   */
  private var volume: Float = 1f

  fun init(dir: File) {
    if (soundPool == null) {
      cacheDir = dir

      soundPool = SoundPool.Builder().setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build()
      ).setMaxStreams(12).build()

      soundPool?.setOnLoadCompleteListener { _, soundId, _ ->
        if (soundCallBackMap.containsKey(soundId)) {
          soundCallBackMap[soundId]?.onComplete(soundId)
        }
      }
    }
  }


  fun release() {
    if (soundCallBackMap.isNotEmpty()) {
      soundCallBackMap.clear()
    }
    soundPool?.release()
    soundPool = null
  }

  /**
   * 根据当前播放实体，设置音量
   *
   * @param volume 范围在 [0, 1]
   * @param entity 根据需要控制对应 entity 音量大小，若为空则控制所有正在播放的音频音量
   */
  fun setVolume(volume: Float, entity: SVGAVideoEntity? = null) {
    if (volume < 0f || volume > 1f) {
      return
    }

    if (entity == null) {
      SVGASoundManager.volume = volume
      val iterator = soundCallBackMap.entries.iterator()
      while (iterator.hasNext()) {
        val e = iterator.next()
        e.value.onVolumeChange(volume)
      }
      return
    }

    val soundPool = soundPool ?: return

    entity.audioMap.forEach { audio ->
      val streamId = audio.value.playID ?: return
      soundPool.setVolume(streamId, volume, volume)
    }
  }

  internal fun load(
    callBack: SoundCallBack?,
    path: String
  ): Int {
    return soundPool?.load(path, 1)?.also {
      if (callBack != null && !soundCallBackMap.containsKey(it)) {
        soundCallBackMap[it] = callBack
      }
    } ?: -1
  }

  internal fun unload(soundId: Int) {
    soundCallBackMap.remove(soundId)
    soundPool?.apply {
      stop(soundId)
      unload(soundId)
    }
  }

  internal fun play(soundId: Int?): Int {
    if (soundPool == null || soundId == null) {
      return -1
    }
    return soundPool?.play(soundId, volume, volume, 1, 0, 1.0f) ?: -1
  }

  internal fun stop(soundId: Int?) {
    soundId?.apply {
      soundPool?.stop(this)
    }
  }

  internal fun resume(soundId: Int) {
    soundPool?.resume(soundId)
  }

  internal fun pause(soundId: Int) {
    soundPool?.pause(soundId)
  }
}