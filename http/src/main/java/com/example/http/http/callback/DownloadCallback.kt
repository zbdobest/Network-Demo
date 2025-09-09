package com.example.http.http.callback

import com.example.http.http.model.Progress
import java.io.File

/**
 * 文件下载回调接口
 */
interface DownloadCallback {
    fun onProgress(progress: Progress)
    fun onSuccess(file: File)
    fun onError(code: Int, message: String)
    fun onException(throwable: Throwable)
}
