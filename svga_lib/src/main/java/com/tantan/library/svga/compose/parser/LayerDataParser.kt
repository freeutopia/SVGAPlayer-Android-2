package com.tantan.library.svga.compose.parser

import com.tantan.library.svga.compose.entities.SVGAFrame
import com.tantan.library.svga.compose.entities.SVGALayer
import com.tantan.library.svga.compose.proto.FrameEntity
import com.tantan.library.svga.compose.proto.MovieEntity

/**
 * frequency : 每隔frequency帧，就减去一帧
 * frequency > 0 ：开启跳帧模式
 */
internal class LayerDataParser : DataParser<MovieEntity, List<SVGALayer>>() {

  override fun onParser(data: MovieEntity): List<SVGALayer> {
    return data.sprites?.map {
      return@map SVGALayer(it.imageKey, it.matteKey, buildFrames(it.frames))
    } ?: listOf()
  }

  private fun buildFrames(data: List<FrameEntity>?): List<SVGAFrame> {
    var lastFrame: SVGAFrame? = null
    return data?.map { entity ->
      val frame = SVGAFrame(entity)
      frame.takeIf {
        it.shapes.isNotEmpty() && it.shapes.first().isKeep
      }?.takeIf { lastFrame != null }?.apply {
        frame.shapes = lastFrame!!.shapes
      }
      lastFrame = frame
      return@map frame
    } ?: listOf()
  }
}