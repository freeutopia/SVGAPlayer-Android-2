package com.utopia.svga.compose.parser

internal abstract class DataParser<From, To> {

  open fun parser(data: From, onReady: (To) -> Unit) {
    onParser(data).let(onReady)
  }

  abstract fun onParser(data: From): To
}