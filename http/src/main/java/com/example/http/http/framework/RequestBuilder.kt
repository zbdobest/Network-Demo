package com.example.http.http.framework

import java.io.File

/**
 * 网络请求构建器（简化版，专注于构建请求参数）
 */
class RequestBuilder {
    private val headers: MutableMap<String, String> = mutableMapOf()
    private val params: MutableMap<String,@JvmSuppressWildcards Any> = mutableMapOf()

    fun addHeader(key: String, value: String): RequestBuilder {
        headers[key] = value
        return this
    }

    fun addParam(key: String, value: Any): RequestBuilder {
        params[key] = value
        return this
    }

    fun addHeaders(headers: Map<String, String>): RequestBuilder {
        this.headers.putAll(headers)
        return this
    }

    fun addParams(params: Map<String,@JvmSuppressWildcards Any>): RequestBuilder {
        this.params.putAll(params)
        return this
    }

    fun buildHeaders(): Map<String, String> = headers.toMap()

    fun buildParams(): Map<String,@JvmSuppressWildcards Any> = params.toMap()

    fun clear() {
        headers.clear()
        params.clear()
    }
}