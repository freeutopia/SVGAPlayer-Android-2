package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import com.utopia.svga.ResourceKey;
import com.utopia.svga.SVGALoader;
import com.utopia.svga.SVGAnimationView;
import com.utopia.svga.data.cache.Resource;
import com.utopia.svga.data.request.RequestCallback;
import com.utopia.svga.exception.SVGAException;
import org.jetbrains.annotations.NotNull;

public class AnimationFromNetworkActivity extends Activity {

  SVGAnimationView animationView = null;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    animationView = new SVGAnimationView(this);
    animationView.setBackgroundColor(Color.GRAY);
    setContentView(animationView);
    loadAnimation();

    // core_card_swipe_like.svga.so=https://auto.tancdn.com/v1/raw/a982f15f-30aa-47be-9b7e-50ec88482f5311.so

    // core_card_swipe_dislike.svga.so=https://auto.tancdn.com/v1/raw/da3d1a53-b937-41f8-883c-c16c842ca35e11.so
  }

  private void loadAnimation() {
    animationView.setRequestCallback(
        new RequestCallback() {
          @Override
          public void onLoadFailed(
              @NotNull ResourceKey key, @org.jetbrains.annotations.Nullable SVGAException e) {
            e.printStackTrace();
            Log.e("lt-log", "加载失败！");
          }

          @Override
          public void onResourceReady(@NotNull ResourceKey key, @NotNull Resource<?> resource) {
            Log.e("lt-log", "加载成功！");
          }
        });
    SVGALoader.with(this)
        // .from("https://github.com/svga/SVGAPlayer-Android/blob/master/app/src/main/assets/mp3_to_long.svga")
        // .from("https://github.com/yyued/SVGA-Samples/blob/master/posche.svga?raw=true")
        .from("https://auto.tancdn.com/v1/raw/57b9975a-579d-4135-a5cd-f1b4a399904b11.pdf")
        .error("boosting.svga")
        .into(animationView);
  }
}
