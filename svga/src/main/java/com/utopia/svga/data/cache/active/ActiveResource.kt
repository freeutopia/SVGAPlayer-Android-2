package com.utopia.svga.data.cache.active

import com.utopia.svga.data.cache.Resource
import java.util.concurrent.ConcurrentHashMap

class ActiveResource {
  private val data = ConcurrentHashMap<String, Resource<*>>()

  @Synchronized
  fun activate(key: String, value: Resource<*>) {
    data.takeIf { get(key) == null }?.apply {
      put(key, value)
    }
  }

  @Synchronized
  fun deactivate(key: String): Resource<*>? {
    return data.remove(key)
  }

  @Synchronized
  operator fun get(key: String): Resource<*>? {
    return data[key]
  }
}