package com.utopia.svga.data.cache

interface Resource<Z> {
  val resourceClass: Class<Z>

  fun get(): Z

  fun getAbsolutePath(): String?

  val size: Long

  fun acquire()//设置活动资源

  fun release()//回收活动资源
}