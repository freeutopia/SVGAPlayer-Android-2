package com.utopia.svga.data.fetcher

import android.content.res.AssetManager
import java.io.IOException
import java.io.InputStream

class StreamAssetResourceFetcher(assetManager: AssetManager?, assetPath: String) :
  AssetResourceFetcher<InputStream>(assetManager, assetPath) {

  override fun loadResource(assetManager: AssetManager?, path: String): InputStream? =
    assetManager?.open(path)

  @Throws(IOException::class)
  override fun close(data: InputStream?) {
    data?.close()
  }

}