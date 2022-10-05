package com.utopia.svga.data.cache

interface Key {
  fun get(): String
  fun cacheKey(): String
}