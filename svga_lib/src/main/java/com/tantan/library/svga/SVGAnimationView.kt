package com.tantan.library.svga

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.RESTART
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.tantan.library.svga.compose.SVGADynamicEntity
import com.tantan.library.svga.compose.SVGAVideoEntity
import com.tantan.library.svga.data.cache.Resource
import com.tantan.library.svga.data.request.RequestCallback
import com.tantan.library.svga.drawer.SVGADrawable
import com.tantan.library.svga.utils.Log
import com.tantan.library.svga.utils.SVGAExecutors
import java.util.*

open class SVGAnimationView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = 0
) : ImageView(context, attrs, defStyleAttr, defStyleRes), ValueAnimator.AnimatorUpdateListener,
  Animator.AnimatorListener, Animator.AnimatorPauseListener {
  enum class FrameMode {
    AFTER,
    BEFORE
  }

  private var mResource: Resource<SVGAVideoEntity>? = null
  private var mResourceKey: ResourceKey? = null
  private var mDynamic: SVGADynamicEntity? = null
  internal var mLoops = 0
    set(value) {
      field = if (value < 0) 0 else value
    }
  internal var mAutoPlay = true
  internal var mFillMode: FrameMode = FrameMode.BEFORE
  internal var audioEnable: Boolean = false
  var animListener: AnimListener? = null
  var requestCallback: RequestCallback? = null
  private var mItemClickAreaListener: SVGAClickAreaListener? = null
  private val mAnimator = ValueAnimator.ofInt(0, 1)

  internal fun setResource(
    resource: Resource<SVGAVideoEntity>?,
    dynamic: SVGADynamicEntity?
  ) {
    mDynamic = dynamic
    if (isAttachedToWindow) {
      mResource = resource
      fillData()
    } else {
      resource?.release()
      mResourceKey = resource?.get()?.key
    }
  }

  private fun fillData() {
    mResource?.get()?.apply {
      setImageDrawable(SVGADrawable(this@SVGAnimationView, this, mDynamic))
      initAnimation(fps, frames)
      checkAndParserAudio(audioEnable) {
        if (mAutoPlay) {
          startAnimation()
        } else {
          drawLastFrame()
        }
      }
      requestLayout()
    } ?: setImageDrawable(null)
  }

  fun clearDynamicData() {
    mDynamic?.clearDynamicObjects()
  }

  private fun initAnimation(fps: Int, frames: Int) {
    mAnimator.apply {
      setIntValues(0, frames - 1)
      interpolator = LinearInterpolator()
      duration = 1000L * frames / fps
      repeatCount = if (mLoops > 0) mLoops - 1 else -1
      repeatMode = RESTART
      currentPlayTime = 0L
    }
  }

  private fun getSVGADrawable() = drawable as? SVGADrawable

  fun startAnimation() {
    mAutoPlay = true
    mAnimator.apply {
      if (isPaused) {
        resume()
      } else if (!isRunning) {
        repeatCount = if (mLoops > 0) mLoops - 1 else -1
        currentPlayTime = 0L
        start()
      }
    }
  }

  public fun pauseAnimation() {
    mAnimator.pause()
    animListener?.onPause()
  }

  @Deprecated("use #startAnimation() instead")
  public fun resumeAnimation() {
    mAnimator.resume()
    animListener?.onStart()
  }

  public fun isPaused(): Boolean {
    return mAnimator.isPaused
  }

  public fun isAnimating(): Boolean {
    return !isPaused() && (mAnimator.isRunning)
  }

  /**
   * 注意：通过stopAnimation(true)结束的动画，会附带回收资源的操作，无法恢复动画
   */
  fun stopAnimation(clear: Boolean = false) {
    if (!isAnimating()) {
      return
    }

    SVGAExecutors.postOnUiThread {
      mAnimator.cancel()
    }
  }

  fun stepToFrame(percent: Float, andPlay: Boolean) {
    if (isAnimating() && !andPlay) {
      pauseAnimation()
    }
    mAnimator.let {
      it.currentPlayTime = (0.0f.coerceAtLeast(1.0f.coerceAtMost(percent)) * it.duration).toLong()
    }.takeIf { andPlay && !isAnimating() }?.apply { startAnimation() }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (event.action != MotionEvent.ACTION_DOWN) {
      return super.onTouchEvent(event)
    }

    mDynamic?.let {
      for ((key, value) in it.mClickMap) {
        if (event.x >= value[0] && event.x <= value[2] && event.y >= value[1] && event.y <= value[3]) {
          mItemClickAreaListener?.onClick(key)
        }
      }
    }

    return super.onTouchEvent(event)
  }

  fun setOnClickListener(clickListener: SVGAClickAreaListener) {
    this.mItemClickAreaListener = clickListener
  }

  override fun onDetachedFromWindow() {
    mAnimator.let {
      it.removeAllListeners()
      it.removeAllUpdateListeners()
    }
    super.onDetachedFromWindow()

    mResource?.apply {
      mAutoPlay = isAnimating()
      mResource = null
      mResourceKey = get().key
      release()
    }

    mAnimator.cancel()
    setImageDrawable(null)
  }

  override fun onAttachedToWindow() {
    mAnimator.let {
      it.addUpdateListener(this)
      it.addPauseListener(this)
      it.addListener(this)
    }
    super.onAttachedToWindow()
    mResourceKey?.takeIf { mResource == null }?.let {
      mResourceKey = null
      SVGALoader.with(context).startRequest(this, it, mDynamic)
    }
  }


  /**
   * 清理该View所有持有资源
   */
  internal fun clearHistory() {
    mResource?.release()
    mResource = null
    mDynamic = null
    mResourceKey = null
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    mResource?.get()?.apply {
      var w = videoSize.width()
      var h = videoSize.height()

      w += paddingLeft + paddingRight
      h += paddingTop + paddingBottom

      w = w.coerceAtLeast(suggestedMinimumWidth)
      h = h.coerceAtLeast(suggestedMinimumHeight)

      val widthSize = resolveSizeAndState(w, widthMeasureSpec, 0)
      val heightSize = resolveSizeAndState(h, heightMeasureSpec, 0)
      setMeasuredDimension(widthSize, heightSize)

      resizeBitmap(widthSize, heightSize)
    } ?: super.onMeasure(widthMeasureSpec, heightMeasureSpec)
  }

  override fun onAnimationUpdate(animation: ValueAnimator?) {
    getSVGADrawable()?.run {
      currentFrame = animation?.animatedValue as Int
      this@SVGAnimationView.animListener?.onStep(currentFrame)
    }
  }

  override fun onAnimationRepeat(animation: Animator?) {
    animListener?.onRepeat()
  }

  override fun onAnimationEnd(animation: Animator?) {
    drawLastFrame()
    animListener?.onFinished()
  }

  override fun onAnimationCancel(animation: Animator?) {
  }

  override fun onAnimationStart(animation: Animator?) {
    animListener?.onStart()
  }

  private fun drawLastFrame() {
    mAnimator.let {
      it.currentPlayTime = if (mFillMode == FrameMode.AFTER) 0 else it.duration
    }
  }

  override fun onAnimationPause(animation: Animator?) {
    getSVGADrawable()?.pause()
  }

  override fun onAnimationResume(animation: Animator?) {
    getSVGADrawable()?.resume()
  }
}