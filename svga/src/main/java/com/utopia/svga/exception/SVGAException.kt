package com.utopia.svga.exception

class SVGAException @JvmOverloads constructor(
  message: String,
  cause: Throwable? = null
) : Throwable(message, cause) {

  constructor(e: Throwable) : this("SVGAException", e)

}