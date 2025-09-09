package com.example.http.http.dsl

class NetworkRequestDSL<T> {

    var onSuccess: ((T) -> Unit)? = null

    var onError: ((Int, String) -> Unit)? = null

    var onException: ((Throwable) -> Unit)? = null


    fun onSuccess(block: (T) -> Unit) {
        onSuccess = block
    }

    fun onError(block: (Int, String) -> Unit) {
        onError = block
    }

    fun onException(block: (Throwable) -> Unit) {
        onException = block
    }
}