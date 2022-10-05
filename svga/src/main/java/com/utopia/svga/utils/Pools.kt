package com.utopia.svga.utils

class Pools private constructor() {

  interface Pool<T> {

    fun acquire(vararg args: Int): T?

    fun release(instance: T): Boolean

  }

  open class ObjectPool<T>(maxPoolSize: Int) : Pool<T> {
    private val mPool: Array<Any?>
    private var mPoolSize = 0

    init {
      require(maxPoolSize > 0) { "The max pool size must be > 0" }
      mPool = arrayOfNulls(maxPoolSize)
    }

    @Suppress("UNCHECKED_CAST")
    override fun acquire(vararg args: Int): T? {
      if (mPoolSize > 0) {
        val lastPooledIndex = mPoolSize - 1
        val instance = mPool[lastPooledIndex] as T?
        mPool[lastPooledIndex] = null
        mPoolSize--
        return instance
      }
      return null
    }

    override fun release(instance: T): Boolean {
      check(!isInPool(instance)) { "Already in the pool!" }
      if (mPoolSize < mPool.size) {
        mPool[mPoolSize] = instance
        mPoolSize++
        return true
      }
      return false
    }

    private fun isInPool(instance: T): Boolean {
      for (i in 0 until mPoolSize) {
        if (mPool[i] === instance) {
          return true
        }
      }
      return false
    }
  }
}