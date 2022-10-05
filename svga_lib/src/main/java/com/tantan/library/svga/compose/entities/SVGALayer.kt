package com.tantan.library.svga.compose.entities

internal data class SVGALayer(
  val imageKey: String?,
  val matteKey: String?,
  val frames: List<SVGAFrame>
)
