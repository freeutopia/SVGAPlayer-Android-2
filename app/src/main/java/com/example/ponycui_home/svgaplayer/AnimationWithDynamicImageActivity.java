package com.example.ponycui_home.svgaplayer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.text.TextPaint;
import androidx.annotation.Nullable;
import com.tantan.library.svga.SVGALoader;
import com.tantan.library.svga.SVGAnimationView;
import com.tantan.library.svga.compose.SVGADynamicEntity;

public class AnimationWithDynamicImageActivity extends Activity {

  SVGAnimationView animationView = null;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    animationView = new SVGAnimationView(this);
    animationView.setBackgroundColor(Color.GRAY);
    setContentView(animationView);
    loadDynamicImageAnimation(
        "https://github.com/PonyCui/resources/blob/master/svga_replace_avatar.png?raw=true");
    // loadDynamicImageAnimation(
    //    "https://auto.tancdn.com/v1/raw/7d782cb5-c7be-4b16-badc-8ed85093b37b07.jpg");
  }

  private void loadDynamicTextAnimation() {
    SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
    TextPaint textPaint = new TextPaint();
    textPaint.setTextSize(40f);
    textPaint.setColor(Color.RED);
    textPaint.setAntiAlias(true);
    textPaint.setTextAlign(Align.CENTER);
    dynamicEntity.setDynamicText("55", textPaint, "N");

    SVGALoader.with(this)
        .from("https://auto.tancdn.com/v1/raw/67b863f4-02da-45bb-8fca-19795bcd71fd11.so")
        .dynamic(dynamicEntity)
        .into(animationView);
  }

  private void loadDynamicImageAnimation(String imageUrl) {
    SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
    dynamicEntity.setDynamicImage(imageUrl, "99");

    SVGALoader.with(this).from("kingset.svga").dynamic(dynamicEntity).into(animationView);
  }
}
