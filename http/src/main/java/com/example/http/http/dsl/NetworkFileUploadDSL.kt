package com.example.http.http.dsl

import com.example.http.http.model.Progress


class NetworkFileUploadDSL {

    var onProgress: ((Progress) -> Unit)? = null
    var onSuccess: ((String) -> Unit)? = null
    var onError: ((Int, String) -> Unit)? = null
    var onException: ((Throwable) -> Unit)? = null

    fun onProgress(block: (Progress) -> Unit) {
        onProgress = block
    }

    fun onSuccess(block: (String) -> Unit) {
        onSuccess = block
    }

    fun onError(block: (Int, String) -> Unit) {
        onError = block
    }

    fun onException(block: (Throwable) -> Unit) {
        onException = block
    }

}