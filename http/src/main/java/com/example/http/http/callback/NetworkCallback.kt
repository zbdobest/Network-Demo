package com.example.http.http.callback

/**
 * 网络请求回调接口
 * @param T 响应数据类型
 */
interface NetworkCallback<T> {
    fun onSuccess(data: T)
    fun onError(code: Int, message: String)
    fun onException(throwable: Throwable)
}