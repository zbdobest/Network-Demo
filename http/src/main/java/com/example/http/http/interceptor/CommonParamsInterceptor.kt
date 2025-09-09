package com.example.http.http.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 公共参数拦截器
 */
class CommonParamsInterceptor(private val commonParams: Map<String, Any>) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        val urlBuilder = originalHttpUrl.newBuilder()
        commonParams.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value.toString())
        }

        val newRequest = originalRequest.newBuilder()
            .url(urlBuilder.build())
            .build()

        return chain.proceed(newRequest)
    }
}