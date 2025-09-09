package com.example.http.http.model

/**
 * 网络请求响应基类
 * @param T 数据类型
 * @param error_code 状态码
 * @param reason 提示信息
 * @param result 数据
 */
data class BaseResponse<T>(
    val error_code: Int,
    val reason: String,
    val result: T?
)