package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.tantan.library.svga.SVGALoader;
import com.tantan.library.svga.SVGAnimationView;
import com.tantan.library.svga.compose.SVGADynamicEntity;

/** Created by miaojun on 2019/6/21. mail:1290846731@qq.com */
public class AnimationFromClickActivity extends Activity {

  SVGAnimationView animationView = null;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    animationView = new SVGAnimationView(this);
    animationView.setBackgroundColor(Color.WHITE);
    setContentView(animationView);

    loadAnimation();
  }

  private void loadAnimation() {
    SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
    dynamicEntity.setClickArea("img_10");
    SVGALoader.with(this).from("MerryChristmas.svga").dynamic(dynamicEntity).into(animationView);
  }
}
