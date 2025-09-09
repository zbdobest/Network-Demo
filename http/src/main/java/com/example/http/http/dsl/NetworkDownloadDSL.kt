package com.example.http.http.dsl

import android.media.MediaCodec.LinearBlock
import com.example.http.http.model.Progress
import java.io.File

class NetworkDownloadDSL {

    var onProgress: ((Progress) -> Unit)? = null

    var onSuccess: ((File) -> Unit)? = null

    var onError: ((Int, String) -> Unit)? = null

    var onException: ((Throwable) -> Unit)? = null


    fun onProgress(block: (progress: Progress) -> Unit) {
        onProgress = block
    }

    fun onSuccess(block: (file: File) -> Unit) {
        onSuccess = block
    }

    fun onError(block: (code: Int, message: String) -> Unit) {
        onError = block
    }

    fun onException(block: (throwable: Throwable) -> Unit) {
        onException = block
    }
}