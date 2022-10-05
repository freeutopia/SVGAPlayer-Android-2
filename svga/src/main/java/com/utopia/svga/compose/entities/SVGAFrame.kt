package com.utopia.svga.compose.entities

import com.utopia.svga.compose.proto.FrameEntity
import com.utopia.svga.utils.FRect
import com.utopia.svga.utils.FMatrix

class SVGAFrame(obj: FrameEntity) {
  var alpha = obj.alpha ?: 0.0f
  var layout = FRect(0f, 0f, 1f, 1f)
  var matrix = FMatrix()
  var maskPath: SVGAPathEntity? = null
  var shapes: List<SVGAVideoShapeEntity>

  init {
    obj.layout?.let {
      this.layout.transform(it)
    }

    obj.transform?.let {
      this.matrix.transform(it)
    }

    obj.clipPath?.takeIf { it.isNotEmpty() }?.let {
      this.maskPath = SVGAPathEntity(it)
    }

    this.shapes = obj.shapes?.map {
      return@map SVGAVideoShapeEntity(it)
    } ?: listOf()
  }

}
