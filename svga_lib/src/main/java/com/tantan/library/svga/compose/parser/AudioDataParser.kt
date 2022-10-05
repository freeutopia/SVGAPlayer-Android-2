package com.tantan.library.svga.compose.parser

import com.tantan.library.svga.utils.SVGASoundManager
import com.tantan.library.svga.SoundCallBack
import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.compose.entities.SVGAAudio
import com.tantan.library.svga.compose.proto.MovieEntity
import com.tantan.library.svga.utils.ByteBufferUtil
import java.io.File
import java.nio.ByteBuffer

internal class AudioDataParser(private val entity: SVGAVideoEntity) :
  DataParser<MovieEntity, Map<File, SVGAAudio>>() {
  private var soundCallback: SoundCallBack? = null
  private val audioMap = mutableMapOf<File, SVGAAudio>()

  override fun parser(data: MovieEntity, onReady: (Map<File, SVGAAudio>) -> Unit) {
    soundCallback = object : SoundCallBack {
      var count = 0
      override fun onVolumeChange(value: Float) {
        SVGASoundManager.setVolume(value, entity)
      }

      override fun onComplete(soundId: Int) {
        if (++count >= audioMap.size) {
          onReady(audioMap)
        }
      }
    }
    onParser(data)
  }

  override fun onParser(data: MovieEntity): Map<File, SVGAAudio> {
    val audiosFileMap = generateAudioFileMap(data)
    if (audiosFileMap.isEmpty()) {
      soundCallback?.onComplete(-1)
      return audioMap
    }

    audioMap.clear()
    data.audios.map { audio ->
      audiosFileMap[audio.audioKey]?.let { file ->
        val item = SVGAAudio(audio)
        item.soundID = SVGASoundManager.load(
          soundCallback,
          file.absolutePath
        )
        audioMap.put(file, item)
      }
    }

    if (audioMap.isEmpty()) {
      soundCallback?.onComplete(-1)
    }
    return audioMap
  }

  private fun generateAudioFileMap(entity: MovieEntity): HashMap<String, File> {
    val audiosFileMap = HashMap<String, File>()
    generateAudioMap(entity).map { audio ->
      val audioCache = File(SVGASoundManager.cacheDir, "${audio.key}.mp3")
      audiosFileMap[audio.key] =
        audioCache.takeIf { it.exists() } ?: generateAudioFile(audioCache, audio.value)
    }
    return audiosFileMap
  }

  private fun generateAudioFile(audioCache: File, value: ByteArray): File {
    ByteBufferUtil.toFile(ByteBuffer.wrap(value), audioCache)
    return audioCache
  }

  private fun generateAudioMap(entity: MovieEntity): HashMap<String, ByteArray> {
    val audiosDataMap = HashMap<String, ByteArray>()
    entity.images?.entries?.forEach {
      val imageKey = it.key
      val byteArray = it.value.toByteArray()
      if (byteArray.count() < 4) {
        return@forEach
      }
      val fileTag = byteArray.slice(IntRange(0, 3))
      if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) {
        audiosDataMap[imageKey] = byteArray
      } else if (fileTag[0].toInt() == -1 && fileTag[1].toInt() == -5 && fileTag[2].toInt() == -108) {
        audiosDataMap[imageKey] = byteArray
      }
    }
    return audiosDataMap
  }
}