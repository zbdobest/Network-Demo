package com.example.http.http.framework

import com.example.http.http.callback.ProgressListener
import java.io.File

/**
 * 网络请求封装
 */
data class NetworkRequest<T>(
    val apiService: Class<T>?,
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val params: Map<String, Any>,
    val body: Any?,
    val files: Map<String, File>,
    val downloadPath: String,
    val progressListener: ProgressListener?
)