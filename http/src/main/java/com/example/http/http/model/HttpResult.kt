package com.example.http.http.model

/**
 * 网络请求结果封装
 * @param T 数据类型
 */
sealed class HttpResult<out T> {
    data class Success<out T>(val data: T) : HttpResult<T>()
    data class Error(val code: Int, val message: String) : HttpResult<Nothing>()
    data class Exception(val throwable: Throwable) : HttpResult<Nothing>()
}