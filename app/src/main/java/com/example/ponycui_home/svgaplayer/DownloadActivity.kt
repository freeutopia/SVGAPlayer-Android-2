package com.example.ponycui_home.svgaplayer

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.tantan.library.svga.ResourceKey
import com.tantan.library.svga.SVGALoader
import com.tantan.library.svga.data.cache.Resource
import com.tantan.library.svga.data.request.RequestCallback
import com.tantan.library.svga.exception.SVGAException

class DownloadActivity : Activity(), RequestCallback {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_download)
    val keys = mutableListOf<ResourceKey>()
    keys.add(
      ResourceKey.KeyBuilder()
        .path("https://github.com/PonyCui/resources/blob/master/svga_replace_avatar.png?raw=true")
        .build()
    )
    keys.add(
      ResourceKey.KeyBuilder()
        .path("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true")
        .build()
    )
    keys.add(
      ResourceKey.KeyBuilder()
        .path("https://github.com/svga/SVGAPlayer-Android/blob/master/app/src/main/assets/Castle.svga")
        .build()
    )
    keys.add(
      ResourceKey.KeyBuilder()
        .path("https://github.com/svga/SVGAPlayer-Android/blob/master/app/src/main/assets/rose.svga")
        .build()
    )
    keys.add(
      ResourceKey.KeyBuilder()
        .path("https://github.com/svga/SVGAPlayer-Android/blob/master/app/src/main/assets/mp3_to_long.svga")
        .build()
    )

    findViewById<Button>(R.id.button).setOnClickListener {
      SVGALoader.with(this)
        .from("https://auto.tancdn.com/v1/raw/eccc8cc2-23f7-41b1-a3bd-b6fc48d0183d11.so")
        .loadCallback(object : RequestCallback {
          override fun onResourceReady(key: ResourceKey, resource: Resource<*>) {
            Log.e("lt-log", "path:" + resource.getAbsolutePath())
          }

          override fun onLoadFailed(key: ResourceKey, e: SVGAException?) {

          }
        })
        .downloadOnly()
      SVGALoader.with(this@DownloadActivity)
        .from("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true")
        .loadCallback(this)
        .downloadOnly()

      /*Svga.with(this@DownloadActivity)
            .load("https://github.com/PonyCui/resources/blob/master/svga_replace_avatar.png?raw=true")
            .preload(object {
              override fun onLoadSuccess(fileName: String?) {
                Log.e("lt-log", "download success:$fileName")
              }

              override fun onLoadFailed(e: SvgaException?) {
                Log.e("lt-log", "download failed")
                e?.printStackTrace()
              }

            })*/
    }
  }

  override fun onResourceReady(key: ResourceKey, resource: Resource<*>) {
    Log.e("lt-log", "我加载成功了:" + key.path)
  }

  override fun onLoadFailed(key: ResourceKey, e: SVGAException?) {
    Log.e("lt-log", "我加载失败了:" + key.path)
    e?.printStackTrace()
  }
}