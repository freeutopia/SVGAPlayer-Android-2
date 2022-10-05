package com.example.ponycui_home.svgaplayer;

import android.content.res.Resources;

public final class DpUtil {

  private DpUtil() {
    /*私有*/
  }

  private static int sWidthPixels;
  private static int sActionBarSize;

  /** 根据手机的分辨率从 dp 的单位 转成为 px(像素) */
  public static int dp2px(float dpValue) {
    final float scale = Resources.getSystem().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  /** 根据手机的分辨率从 px(像素) 的单位 转成为 dp */
  public static int px2dp(float pxValue) {
    final float scale = Resources.getSystem().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }

  /** sp转换成px */
  public static int sp2px(float spValue) {
    float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }

  /** px转换成sp */
  public static int px2sp(float pxValue) {
    float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
    return (int) (pxValue / fontScale + 0.5f);
  }

  /** 获取屏幕宽度 */
  public static int displayWidth() {
    if (sWidthPixels == 0) {
      sWidthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
    }
    return sWidthPixels;
  }

  public static int getActionBarSize() {
    if (sActionBarSize == 0) {
      sActionBarSize = dp2px(56);
    }
    return sActionBarSize;
  }
}
