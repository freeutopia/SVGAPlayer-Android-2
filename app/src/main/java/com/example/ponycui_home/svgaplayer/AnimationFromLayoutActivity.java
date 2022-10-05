package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import com.utopia.svga.SVGALoader;
import com.utopia.svga.SVGAnimationView;
import com.utopia.svga.SVGAnimationView.FrameMode;

/** Created by cuiminghui on 2017/3/30. 将 svga 文件打包到 assets 文件夹中，然后使用 layout.xml 加载动画。 */
public class AnimationFromLayoutActivity extends Activity {
  private int currentIndex = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_simple);
    SVGAnimationView animationView1 = findViewById(R.id.svga_view_1);
    SVGAnimationView animationView2 = findViewById(R.id.svga_view_2);
    // loadAnimation(animationView1);
    // loadAnimation(animationView2);

    SVGALoader.with(this)
        .from(SVGAResourceUtils.CORE_QUICKCHAT_PULL_HEY)
        .autoPlay(false)
        .repeatCount(2)
        .frameMode(FrameMode.BEFORE)
        .into(animationView1);
    animationView1.startAnimation();
    RelativeLayout root = findViewById(R.id.root);
    findViewById(R.id.btn_load)
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (animationView1.isAttachedToWindow()) {
                  root.removeView(animationView1);
                } else {
                  root.addView(animationView1);
                }
              }
            });
    findViewById(R.id.btn)
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (animationView1.isAnimating()) {
                  animationView1.stopAnimation(true);
                } else {
                  animationView1.startAnimation();
                }
              }
            });
    /*SVGALoader.Companion.with(this)
    .from("https://auto.tancdn.com/v1/raw/135e8823-4fef-4105-9f7d-42b2034c9c6f09.so")
    // .repeatCount(-1)
    .frameMode(FrameMode.AFTER)
    .autoPlay(false)
    .into(animationView);*/
    /*animationView.setVisibility(View.VISIBLE);
    animationView.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (animationView.isAnimating()) {
              animationView.stopAnimation(false);
            } else {
              animationView.startAnimation();
            }
          }
        });
    animationView.setAnimListener(
        new AnimListener() {
          @Override
          public void onStart() {
            Log.e("lt-log", "onStart");
          }

          @Override
          public void onPause() {
            Log.e("lt-log", "onPause");
          }

          @Override
          public void onFinished() {
            Log.e("lt-log", "onFinished");
          }

          @Override
          public void onRepeat() {
            Log.e("lt-log", "onRepeat");
          }

          @Override
          public void onStep(int frame) {
            Log.e("lt-log", "onStep:" + frame);
          }
        });*/
  }

  private void loadAnimation(SVGAnimationView view) {
    SVGALoader.with(this)
        // .from("MerryChristmas.svga")
        .from("https://auto.tancdn.com/v1/raw/4c5c00eb-a044-40e7-9e1a-35c5dbbf610411.svga")
        .autoPlay(true)
        .audioEnable(false)
        .repeatCount(2)
        // .isCacheable(true)
        .frameMode(FrameMode.BEFORE)
        .into(view);
  }
}
