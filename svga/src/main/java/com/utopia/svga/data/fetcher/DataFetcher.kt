package com.utopia.svga.data.fetcher

import java.io.IOException

interface DataFetcher<T> {
  @Throws(IOException::class)
  fun loadData(): T?//加载

  fun recycle()//回收
}