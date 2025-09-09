package com.example.http.http.utils

import android.util.Log
import com.example.http.BuildConfig

/**
 * 日志工具类
 */
object Logger {
    private const val TAG = "NetworkFramework"
    private var isDebug = BuildConfig.DEBUG

    fun setDebug(debug: Boolean) {
        isDebug = debug
    }

    fun d(message: String) {
        if (isDebug) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        if (isDebug) {
            Log.i(TAG, message)
        }
    }

    fun w(message: String) {
        if (isDebug) {
            Log.w(TAG, message)
        }
    }

    fun e(message: String) {
        if (isDebug) {
            Log.e(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable) {
        if (isDebug) {
            Log.e(TAG, message, throwable)
        }
    }
}