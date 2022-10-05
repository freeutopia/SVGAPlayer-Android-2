package com.example.ponycui_home.svgaplayer;

import android.app.Application;
import androidx.annotation.NonNull;
import com.utopia.svga.SVGA;
import com.utopia.svga.SVGAConfig;
import com.utopia.svga.data.cache.Key;
import com.utopia.svga.tracker.CacheTrackerListener;
import com.utopia.svga.tracker.CacheType;
import com.utopia.svga.utils.Log;
import kotlin.Pair;

public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    SVGA.init(
        this,
        new SVGAConfig.Builder()
            .setDiskCacheDir(getFilesDir())
            .enableCacheTrace(false)
            .setTrackerListener(
                new CacheTrackerListener() {
                  @Override
                  public void onCacheRemoved(@NonNull CacheType type, @NonNull Key key) {
                    // Log.e(key.get() + " 从" + type.value() + "移除：" + type.value());
                  }

                  @Override
                  public void onCachePut(
                      @NonNull CacheType type, @NonNull Key key, @NonNull Pair<Long, Long> size) {
                    /* Log.e(
                    key.get()
                        + " 加入"
                        + type.value()
                        + "："
                        + size.getFirst()
                        + ",整个'"
                        + type.value()
                        + "'占用"
                        + size.getSecond());*/
                  }

                  @Override
                  public void onCacheHit(@NonNull CacheType type, @NonNull Key key) {
                    Log.e(key.get() + " 命中->" + type.value());
                  }

                  @Override
                  public void onCacheMiss(@NonNull CacheType type, @NonNull Key key) {
                    // Log.e(key.get() + " 未命中->" + type.value());
                  }
                })
            .build());
  }
}
