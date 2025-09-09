package com.example.http.http.interceptor

import com.example.http.http.callback.ProgressListener
import com.example.http.http.model.Progress
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer

/**
 * 进度响应体
 */
class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val listener: ProgressListener
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? = responseBody.contentType()
    override fun contentLength(): Long = responseBody.contentLength()

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            val totalBytes = contentLength()

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0

                val progress = if (totalBytes > 0) {
                    (totalBytesRead * 100 / totalBytes).toInt()
                } else {
                    0
                }

                listener.onProgress(Progress(totalBytesRead, totalBytes, progress))
                return bytesRead
            }
        }
    }
}