package com.utopia.svga.compose.entities

internal data class SVGALayer(
  val imageKey: String?,
  val matteKey: String?,
  val frames: List<SVGAFrame>
)
