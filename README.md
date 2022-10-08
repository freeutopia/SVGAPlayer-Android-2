# SVGAPlayer-Android-2
SVGAPlayer-Android -2 使用原生 Android Canvas 库渲染动画，为你提供高性能、低开销的动画体验。

2.SVGA动画总体实现原理：

SVGA动画引擎 先将动画 protobuf 资源转换为 SVGAVideoEntity 数据对象。
继承 ImageView 的SVGAnimationView 将数据对象 SVGAVideoEntity 和渲染能力委托给SVGADrawable 处理。
在 SVGADrawable 中会将数据对象 SVGAVideoEntity 组建为具有 draw 能力的SVGACanvasDrawer(实际渲染逻辑处理类)。并在 SVGAnimationView 需要绘制时，调用自己和各个层级SVGALayer 的渲染，从而达到动画效果。




下面原理细分为加载、解析、绘制、等完整动画原理进行阐述。


3.模块设计：

1、SVGA加载模块

（1）资源查找路径，延此路线依次尝试发现对应的缓存资源；资源回调路径，此时已经发现了资源，延此路线做缓存变换，最终输出实体

（2）缓存层次委托模型

（3）缓存模块-UML关系图


2、SVGA解析模块

数据解析模块，主要是把动画资源，从File转化为SVGAVideoEntity，供后续步骤使用；

（1）数据变换逻辑图：

（2）数据解析器：




（3）一个完整的SVGAVideoEntity组成：




3、SVGA渲染模块

SVGAnimationView内部通过ValueAnimator驱动动画执行：

mAnimator.apply {
      setIntValues(0, frames - 1)          //设置动画帧区间
      interpolator = LinearInterpolator()  //设置动画线性差值器
      duration = 1000L * frames / fps      //设置动画执行时长
      addUpdateListener { animation ->
        SVGADrawable().run {
          currentFrame = animation.animatedValue //根据回调帧数据，驱动SVGA更新Drawable
        }
      }
}

SVGADrawable → currentFrame变化时触发：invalidateSelf() → draw(canvas)。这时候拿到当前frame和canvas后，便可以执行绘制了


4.使用方式：



（1）在xml中使用:

<com.tantan.library.svga.SVGAnimationView
android:id="@+id/svga_view"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:layout_centerInParent="true"
android:scaleType="centerInside"//scaleType使用方式同ImageView
app:autoPlay="true"//是否自动播放动画
app:frameMode="before"//动画结束后停止帧
app:repeatCount="1"//重复播放次数
app:source="boosting.svga" //资源名称（url或assets/svga下文件名）
/>




（2）在java中使用：

SVGALoader.with(this)
  .from("boosting.svga")
  .error("error.svga")
  .repeatCount(1)
  .autoPlay(false)
  .frameMode(SVGAnimationView.FrameMode.AFTER)
  .into(svgaView)



（3）仅下载：

SVGALoader.with(this)
  .from("https://auto.tancdn.com/v1/raw/eccc8cc2-23f7-41b1-a3bd-b6fc48d0183d11.so")
  .downloadOnly(object :RequestCallback{
    override fun onResourceReady(key: ResourceKey, resource: Resource<*>) {
      TODO("Not yet implemented")
    }

    override fun onLoadFailed(key: ResourceKey, e: SVGAException?) {
      TODO("Not yet implemented")
    }
  })




（4）播放状态监听：

svgaView.setCallback(
    new SVGACallback() {
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
    });




（5）常用api：

停止：stopAnimation（true）:停止播放，并释放动画资源；stopAnimation（false）:停止播放，并释放动画资源，可以通过startAnimation唤起

播放：startAnimation()，开始播放

跳转：stepToFrame(percent: Float, andPlay: Boolean)：percent百分比，andPlay跳转后是否自动播放

是否正在播放中：isAnimating()：true正在播放，false未播放
