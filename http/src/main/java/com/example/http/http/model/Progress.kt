package com.example.http.http.model

/**
 * 文件上传/下载进度回调数据类
 * @param currentBytes 当前字节数
 * @param totalBytes 总字节数
 * @param progress 进度百分比
 */
data class Progress(
    val currentBytes: Long,
    val totalBytes: Long,
    val progress: Int
)