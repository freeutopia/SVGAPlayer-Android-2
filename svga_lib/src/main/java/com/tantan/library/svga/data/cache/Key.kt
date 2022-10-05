package com.tantan.library.svga.data.cache

interface Key {
  fun get(): String
  fun cacheKey(): String
}