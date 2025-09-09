package com.example.http.http.callback

import com.example.http.http.model.Progress


/**
 * 文件上传回调接口
 */
interface UploadCallback {
    fun onProgress(progress: Progress)
    fun onSuccess(url: String)
    fun onError(code: Int, message: String)
    fun onException(throwable: Throwable)
}