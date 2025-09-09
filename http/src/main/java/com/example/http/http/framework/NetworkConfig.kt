package com.example.http.http.framework

/**
 * 网络框架配置类
 */
class NetworkConfig private constructor() {
    var baseUrl: String = ""
    var connectTimeout: Long = 15
    var readTimeout: Long = 15
    var writeTimeout: Long = 15
    var commonParams: Map<String, Any> = emptyMap()
    var commonHeaders: Map<String, String> = emptyMap()
    var isDebug: Boolean = false
    var enableUnsafeSSL:Boolean = isDebug
    var logEnable:Boolean = isDebug

    companion object {
        fun create(): NetworkConfig = NetworkConfig()
    }

    fun baseUrl(url: String): NetworkConfig {
        this.baseUrl = url
        return this
    }

    fun connectTimeout(timeout: Long): NetworkConfig {
        this.connectTimeout = timeout
        return this
    }

    fun readTimeout(timeout: Long): NetworkConfig {
        this.readTimeout = timeout
        return this
    }

    fun writeTimeout(timeout: Long): NetworkConfig {
        this.writeTimeout = timeout
        return this
    }

    fun commonParams(params: Map<String, Any>): NetworkConfig {
        this.commonParams = params
        return this
    }

    fun commonHeaders(headers: Map<String, String>): NetworkConfig {
        this.commonHeaders = headers
        return this
    }

    fun debug(debug: Boolean): NetworkConfig {
        this.isDebug = debug
        return this
    }

    fun enableUnsafeSSL(enableUnsafeSSL: Boolean): NetworkConfig {
        this.enableUnsafeSSL = enableUnsafeSSL
        return this
    }

    fun logEnable(logEnable: Boolean): NetworkConfig {
        this.logEnable = logEnable
        return this
    }
}