package com.example.http.http.callback

import com.example.http.http.model.Progress


/**
 * 进度监听器
 */
interface ProgressListener {
    fun onProgress(progress: Progress)
}