package com.utopia.svga.utils

import android.os.Handler
import android.os.Looper
import com.utopia.svga.exception.GlobalExceptionMonitor
import com.utopia.svga.exception.SVGAException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

internal class SVGAExecutors internal constructor(private val delegate: ExecutorService) {
  internal fun execute(command: ProxyRunnable) {
    delegate.execute(command)
  }

  private class DefaultThreadFactory constructor(private val name: String?) : ThreadFactory {
    private val threadNum = AtomicInteger()
    override fun newThread(runnable: Runnable): Thread {
      val newThread = Thread(runnable)
      newThread.name = "svga-" + name + "-thread-" + threadNum.getAndIncrement()
      return newThread
    }
  }

  class Builder internal constructor() {
    private var corePoolSize = 0
    private var maximumPoolSize = 0
    private var name: String? = null

    fun setThreadCount(threadCount: Int): Builder {
      corePoolSize = threadCount
      maximumPoolSize = threadCount
      return this
    }

    fun setName(name: String?): Builder {
      this.name = name
      return this
    }

    fun build(): SVGAExecutors = SVGAExecutors(
      ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        KEEP_ALIVE_TIME_MS,
        TimeUnit.MILLISECONDS,
        LinkedBlockingDeque(200),
        DefaultThreadFactory(name)
      ) { r, _ ->
        if (r is ProxyRunnable) {
          GlobalExceptionMonitor.get()
            ?.onFailed(r.key, SVGAException("RejectedExecutionException:${r.key}"))
        }
      }
    )
  }

  companion object {
    internal var io =
      Builder().setThreadCount(calculateBestThreadCount()).setName("svga-io").build()
      private set
    private val KEEP_ALIVE_TIME_MS = TimeUnit.SECONDS.toMillis(10)
    private const val MAXIMUM_THREAD_COUNT = 4

    private val mainThreadHandler = Handler(Looper.getMainLooper())

    internal fun postOnUiThread(runnable: Runnable) {
      if (Looper.getMainLooper() == Looper.myLooper()) {
        runnable.run()
      } else {
        mainThreadHandler.post(runnable)
      }
    }

    private fun calculateBestThreadCount(): Int =
      MAXIMUM_THREAD_COUNT.coerceAtMost(Runtime.getRuntime().availableProcessors())
  }
}