package com.tantan.library.svga.tracker;

public enum CacheType {
  NETWORK("网络资源"),
  DISK("磁盘缓存"),
  ASSETS("Assets资源"),
  MEMORY("内存缓存"),
  ACTIVE("活动缓存");

  private final String value;

  private CacheType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
