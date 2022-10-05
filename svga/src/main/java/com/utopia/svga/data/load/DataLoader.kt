package com.utopia.svga.data.load

import com.utopia.svga.ResourceKey
import com.utopia.svga.exception.SVGAException
import com.utopia.svga.tracker.CacheType

abstract class DataLoader<FromSource, ToData>(private val parent: DataLoader<*, FromSource>? = null) {

  @Throws(SVGAException::class)
  open fun loadData(key: ResourceKey): ToData? {
    var ex: Throwable? = null
    var source: FromSource? = null
    //首先，加载本层数据,如果本层加载失败，就调用父加载器的loadData()方法
    try {
      source = findData(key)
    } catch (e: Throwable) {
      ex = e
    }

    //将读取到的数据，转化为本层需要的数据类型
    var data: ToData? = null
    try {
      data = transform(key, source ?: parent?.loadData(key))
    } catch (e: Throwable) {
      ex = e
    }

    //processData()来处理返回结果,如果data==null，则尝试处理异常
    data?.let { processData(key, it) } ?: interceptException(key, ex)

    return data
  }

  /**
   * 通过本层加载数据
   */
  abstract fun findData(key: ResourceKey): FromSource?

  /**
   * 本层加载的对象，转换为上层需要的对象类型
   */
  abstract fun transform(key: ResourceKey, source: FromSource?): ToData?

  /**
   * 处理结果
   */
  abstract fun processData(key: ResourceKey, data: ToData?)

  /**
   * 是否在本层处理异常，否则异常继续传递,false：向下传递，true：本层处理
   */
  @Throws(SVGAException::class)
  open fun interceptException(key: ResourceKey, ex: Throwable?) {
    ex?.let {
      throw if (it is SVGAException) it else SVGAException(it)
    }
  }

  abstract fun getCacheType(): CacheType
}