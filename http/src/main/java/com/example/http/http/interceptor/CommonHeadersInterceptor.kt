package com.example.http.http.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 公共请求头拦截器
 */
class CommonHeadersInterceptor(private val commonHeaders: Map<String, String>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        commonHeaders.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        return chain.proceed(requestBuilder.build())
    }
}