package com.tantan.library.svga.compose.entities

import com.tantan.library.svga.compose.proto.AudioEntity

class SVGAAudio(obj: AudioEntity) {
  val startFrame = obj.startFrame ?: 0
  val endFrame = obj.endFrame ?: 0
  var soundID: Int? = null
  var playID: Int? = null

}