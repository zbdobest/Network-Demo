package com.example.http.http.interceptor

import com.example.http.http.callback.ProgressListener
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 进度拦截器 - 用于文件上传下载进度监听
 */
class ProgressInterceptor(private val listener: ProgressListener?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        listener?.let {
            return response.newBuilder()
                .body(response.body?.let { body ->
                    ProgressResponseBody(body, listener)
                })
                .build()
        }

        return response
    }
}