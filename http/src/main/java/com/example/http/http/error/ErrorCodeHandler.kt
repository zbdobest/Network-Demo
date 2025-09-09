package com.example.http.http.error

import com.example.http.http.model.BaseResponse
import com.example.http.http.model.HttpResult
import com.example.http.http.utils.Logger


/**
 * 错误码处理器
 */
object ErrorCodeHandler {

    private val errorCodeMap = mutableMapOf<Int, String>().apply {
        put(400, "请求参数错误")
        put(401, "未授权，请重新登录")
        put(403, "访问被禁止")
        put(404, "请求的资源不存在")
        put(500, "服务器内部错误")
        put(503, "服务不可用")
        // 可以添加更多错误码映射
    }

    /**
     * 注册自定义错误码
     */
    fun registerErrorCode(code: Int, message: String) {
        errorCodeMap[code] = message
    }

    /**
     * 获取错误信息
     */
    fun getErrorMessage(code: Int): String {
        return errorCodeMap[code] ?: "未知错误 (code: $code)"
    }

    /**
     * 处理错误响应
     */
    fun <T> handleErrorResponse(response: BaseResponse<T>): HttpResult.Error {
        val errorMessage = getErrorMessage(response.error_code)
        Logger.e("Network Error: code=${response.error_code}, message=$errorMessage")
        return HttpResult.Error(response.error_code, errorMessage)
    }
}