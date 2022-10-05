package com.example.ponycui_home.svgaplayer

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import com.tantan.library.svga.AnimListener
import com.tantan.library.svga.SVGALoader
import com.tantan.library.svga.SVGAnimationView
import com.tantan.library.svga.utils.Log

class AnimationFromAssetsActivity : Activity() {
  lateinit var svgaView: SVGAnimationView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    svgaView = SVGAnimationView(this)
    //animationView.scaleType = ImageView.ScaleType.CENTER
    //animationView.setOnClickListener { animationView.stepToFrame(currentIndex++, false) }
    val view = FrameLayout(this)
    view.setBackgroundColor(Color.GRAY)
    //view.addView(svgaView, LayoutParams(500, 500))
    svgaView.scaleType = ImageView.ScaleType.FIT_CENTER
    //view.addView(svgaView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    view.addView(svgaView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    setContentView(view)
    loadAnimation1("https://auto.tancdn.com/v1/raw/3c38989b-e137-43cc-aa81-177e7958cf6910.svga")
    var i = 1
    svgaView.setOnClickListener {
      if (i % 3 == 1) {
        loadAnimation1("https://auto.tancdn.com/v1/raw/d7b4b43d-0e21-4065-9426-2a098571ad3211.svga")
      } else if (i % 3 == 2) {
        loadAnimation1("https://auto.tancdn.com/v1/raw/80bde329-9540-4a40-8b7c-a45577c96b2c11.svga")
      } else {
        loadAnimation1("https://auto.tancdn.com/v1/raw/3c38989b-e137-43cc-aa81-177e7958cf6910.svga")
      }
      i += 1;
    }
  }

  private fun addView(view: SVGAnimationView) {

  }

  /**
   *     this.items.add("https://auto.tancdn.com/v1/raw/f0bd3648-1f28-4912-8450-81792533e95508.so");
  this.items.add("https://auto.tancdn.com/v1/raw/a39aecc6-3012-4a02-97e2-6d9f9198d18008.so");
  this.items.add("https://auto.tancdn.com/v1/raw/135e8823-4fef-4105-9f7d-42b2034c9c6f09.so");
  this.items.add("https://auto.tancdn.com/v1/raw/f4568317-28e5-48d5-9173-1a162743a13909.so");
   */
  private fun loadAnimation1(url: String) {
    svgaView.stopAnimation()
    val startTime = System.currentTimeMillis();
    SVGALoader.with(this)
      .from(url)
      .into(svgaView)
  }
}