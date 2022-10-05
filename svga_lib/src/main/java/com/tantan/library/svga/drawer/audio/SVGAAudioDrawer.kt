package com.tantan.library.svga.drawer.audio

import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.utils.SVGASoundManager

internal class SVGAAudioDrawer(private val videoItem: SVGAVideoEntity) {
  internal fun playAudio(isVisible: Boolean, frameIndex: Int) {
    videoItem.audioMap.values.forEach { audio ->
      if (isVisible && audio.startFrame == frameIndex - 1) {
        audio.playID = SVGASoundManager.play(audio.soundID)
      } else if (!isVisible || audio.endFrame <= frameIndex - 1) {
        SVGASoundManager.stop(audio.playID)
      }
    }
  }

  internal fun resume() {
    videoItem.audioMap.values.forEach { audio ->
      audio.playID?.let { SVGASoundManager.resume(it) }
    }
  }

  internal fun pause() {
    videoItem.audioMap.values.forEach { audio ->
      audio.playID?.let { SVGASoundManager.pause(it) }
    }
  }
}