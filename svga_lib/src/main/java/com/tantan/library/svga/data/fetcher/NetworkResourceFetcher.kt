package com.tantan.library.svga.data.fetcher

import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.net.ProxySelector
import java.net.URL
import java.util.concurrent.TimeUnit

class NetworkResourceFetcher internal constructor(
  private val url: URL,
  private val factory: HttpFactory
) : DataFetcher<InputStream> {

  private var stream: InputStream? = null
  private var defaultHeaders = mapOf(
    Pair("Connection", "close"),
    Pair("Accept-Encoding", "identity")
  )

  constructor(url: URL) : this(url, DEFAULT_HTTP_FACTORY)

  @Throws(IOException::class)
  override fun loadData(): InputStream? {
    stream = loadDataWithUrl(url, defaultHeaders)
    return stream
  }


  @Throws(IOException::class)
  private fun loadDataWithUrl(url: URL, headers: Map<String, String>): InputStream? {

    val reqBuilder = Request.Builder().url(url).get()
    for ((key, value) in headers) {
      reqBuilder.addHeader(key, value)
    }

    val response = factory.get().fetcher(reqBuilder.build())
    return response?.body()?.byteStream()
  }

  override fun recycle() {
    try {
      stream?.close()
    } catch (ignore: IOException) {
      // Ignore
    }
  }

  interface HttpFactory {
    @Throws(IOException::class)
    fun get(): HttpFactory

    @Throws(IOException::class)
    fun fetcher(req: Request): Response?
  }

  private class DefaultHttpFactory : HttpFactory {
    private var client: OkHttpClient? = null

    @Throws(IOException::class)
    override fun get(): HttpFactory {
      if (client == null) {
        client = OkHttpClient.Builder()
          .readTimeout(20, TimeUnit.SECONDS)
          .connectTimeout(20, TimeUnit.SECONDS)
          .addInterceptor(RetryInterceptor(1))
          /*.addInterceptor(
            HttpLoggingInterceptor { message -> Log.e("net-log", message) }.setLevel(
              HttpLoggingInterceptor.Level.BASIC
            )
          )*/
          .proxySelector(ProxySelector.getDefault())
          .build()
      }
      return this
    }

    override fun fetcher(req: Request): Response? {
      return client?.newCall(req)?.execute()
    }
  }

  /**
   * 失败重试机制，在此处细化网络异常日志
   */
  private class RetryInterceptor(val maxRetry: Int = 0) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
      var retryNum = -1
      val request = chain.request()
      var response: Response? = null
      while (retryNum++ < maxRetry) {
        try {
          response = chain.proceed(request)
        } catch (ex: IOException) {
          if (retryNum >= maxRetry) {
            throw ex
          }
        }
        if (response?.isSuccessful == true) {
          return response
        } else {
          response?.close()
        }
      }
      throw IOException("网络请求异常")
    }
  }

  companion object {
    val DEFAULT_HTTP_FACTORY: HttpFactory = DefaultHttpFactory()
  }
}