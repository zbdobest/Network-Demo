package com.example.http.http.framework

import android.annotation.SuppressLint
import com.example.http.http.callback.DownloadCallback
import com.example.http.http.callback.NetworkCallback
import com.example.http.http.callback.ProgressListener
import com.example.http.http.callback.UploadCallback
import com.example.http.http.constants.HttpConstants
import com.example.http.http.error.ErrorCodeHandler
import com.example.http.http.interceptor.CommonHeadersInterceptor
import com.example.http.http.interceptor.CommonParamsInterceptor
import com.example.http.http.interceptor.ProgressInterceptor
import com.example.http.http.model.BaseResponse
import com.example.http.http.model.Progress
import com.example.http.http.ssl.SSLSocketFactoryManager
import com.example.http.http.utils.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 网络框架核心类
 */
object HttpManager {

    private lateinit var retrofit: Retrofit
    private lateinit var okHttpClient: OkHttpClient
    private var isInitialized = false

    /**
     * 初始化网络框架
     * @param config 网络配置
     */
    fun init(config: NetworkConfig) {
        Logger.setDebug(config.isDebug)

        val clientBuilder = OkHttpClient.Builder().apply {
            connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
            readTimeout(config.readTimeout, TimeUnit.SECONDS)
            writeTimeout(config.writeTimeout, TimeUnit.SECONDS)
            addInterceptor(CommonParamsInterceptor(config.commonParams))
            addInterceptor(CommonHeadersInterceptor(config.commonHeaders))
            if (config.enableUnsafeSSL) {
                SSLSocketFactoryManager.applyUnsafeSSLSettings(this)
            }
            if (config.logEnable) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }


        okHttpClient = clientBuilder.build()

        retrofit = Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        isInitialized = true
        Logger.i("NetworkFramework initialized successfully with baseUrl: ${config.baseUrl}")
    }

    /**
     * 创建API服务实例
     * @param service API服务接口类
     * @return API服务实例
     */
    fun <T> createService(service: Class<T>): T {
        checkInitialization()
        return retrofit.create(service)
    }

    /**
     * 执行普通网络请求（使用Retrofit方式）
     * @param call Retrofit Call对象
     * @param callback 网络回调
     */
    fun <T> execute(call: Call<BaseResponse<T>>, callback: NetworkCallback<T>) {
        checkInitialization()

        Logger.d("Executing request: ${call.request().url}")

        call.enqueue(object : Callback<BaseResponse<T>> {
            override fun onResponse(
                call: Call<BaseResponse<T>>,
                response: Response<BaseResponse<T>>
            ) {
                handleResponse(response, callback)
            }

            override fun onFailure(call: Call<BaseResponse<T>>, t: Throwable) {
                callback.onException(t)
                Logger.e("Request failed: ${t.message}", t)
            }
        })
    }

    /**
     * 执行RxJava网络请求
     * @param observable RxJava Observable
     * @param callback 网络回调
     */
    @SuppressLint("CheckResult")
    fun <T> executeRx(observable: Observable<BaseResponse<T>>, callback: NetworkCallback<T>) {
        checkInitialization()

        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                handleBaseResponse(response, callback)
            }, { throwable ->
                callback.onException(throwable)
                Logger.e("RxJava request failed: ${throwable.message}", throwable)
            })
    }

    /**
     * 处理响应
     */
    private fun <T> handleResponse(
        response: Response<BaseResponse<T>>,
        callback: NetworkCallback<T>
    ) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                handleBaseResponse(body, callback)
            } else {
                val errorMessage = "Response body is null"
                callback.onError(-1, errorMessage)
                Logger.e(errorMessage)
            }
        } else {
            val errorMessage = "HTTP error: ${response.code()} - ${response.message()}"
            callback.onError(response.code(), errorMessage)
            Logger.e(errorMessage)
        }
    }

    /**
     * 处理基础响应
     */
    private fun <T> handleBaseResponse(response: BaseResponse<T>, callback: NetworkCallback<T>) {
        if (response.error_code == HttpConstants.HTTP_RESPONSE_CODE_SUCCESS) {
            response.result?.let {
                callback.onSuccess(it)
                Logger.i("Request success: code=${response.error_code}")
            } ?: run {
                val errorMessage = "Data is null in successful response"
                callback.onError(-1, errorMessage)
                Logger.w(errorMessage)
            }
        } else {
            val errorResult = ErrorCodeHandler.handleErrorResponse(response)
            callback.onError(errorResult.code, errorResult.message)
        }
    }

    /**
     * 上传文件（使用OkHttp直接实现）
     * @param url 上传地址
     * @param file 要上传的文件
     * @param params 附加参数
     * @param headers 请求头
     * @param callback 上传回调
     */
    fun upload(
        url: String,
        file: File,
        params: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        callback: UploadCallback
    ) {
        checkInitialization()

        try {
            Logger.d("Starting file upload: $url, file: ${file.name}")

            val client = okHttpClient.newBuilder()
                .addInterceptor(ProgressInterceptor(object : ProgressListener {
                    override fun onProgress(progress: Progress) {
                        callback.onProgress(progress)
                    }
                }))
                .build()

            val requestBody = buildMultipartBody(file, params)
            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)

            // 添加自定义请求头
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            client.newCall(requestBuilder.build()).enqueue(object : okhttp3.Callback {
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        val resultUrl = response.body?.string() ?: ""
                        callback.onSuccess(resultUrl)
                        Logger.i("Upload success: $url")
                    } else {
                        val errorMessage = "Upload failed: ${response.code} - ${response.message}"
                        callback.onError(response.code, errorMessage)
                        Logger.e(errorMessage)
                    }
                }

                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback.onException(e)
                    Logger.e("Upload exception: ${e.message}", e)
                }
            })

        } catch (e: Exception) {
            callback.onException(e)
            Logger.e("Upload preparation exception: ${e.message}", e)
        }
    }

    /**
     * 下载文件
     * @param url 下载地址
     * @param destination 目标文件路径
     * @param headers 请求头
     * @param callback 下载回调
     */
    fun download(
        url: String,
        destination: File,
        headers: Map<String, String> = emptyMap(),
        callback: DownloadCallback
    ) {
        checkInitialization()

        try {
            Logger.d("Starting file download: $url, destination: ${destination.absolutePath}")

            val client = okHttpClient.newBuilder()
                .addInterceptor(ProgressInterceptor(object : ProgressListener {
                    override fun onProgress(progress: Progress) {
                        callback.onProgress(progress)
                    }
                }))
                .build()

            val requestBuilder = Request.Builder()
                .url(url)

            // 添加自定义请求头
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            client.newCall(requestBuilder.build()).enqueue(object : okhttp3.Callback {
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        response.body?.let { body ->
                            try {
                                // 确保目录存在
                                destination.parentFile?.mkdirs()

                                FileOutputStream(destination).use { output ->
                                    body.byteStream().use { input ->
                                        input.copyTo(output)
                                    }
                                }

                                callback.onSuccess(destination)
                                Logger.i("Download success: $url")
                            } catch (e: IOException) {
                                callback.onException(e)
                                Logger.e("File write exception: ${e.message}", e)
                            }
                        } ?: run {
                            val errorMessage = "Download response body is null"
                            callback.onError(-1, errorMessage)
                            Logger.e(errorMessage)
                        }
                    } else {
                        val errorMessage = "Download failed: ${response.code} - ${response.message}"
                        callback.onError(response.code, errorMessage)
                        Logger.e(errorMessage)
                    }
                }

                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback.onException(e)
                    Logger.e("Download exception: ${e.message}", e)
                }
            })

        } catch (e: Exception) {
            callback.onException(e)
            Logger.e("Download preparation exception: ${e.message}", e)
        }
    }

    /**
     * 获取OkHttpClient实例（用于自定义请求）
     */
    fun getOkHttpClient(): OkHttpClient {
        checkInitialization()
        return okHttpClient
    }

    /**
     * 获取Retrofit实例（用于自定义请求）
     */
    fun getRetrofit(): Retrofit {
        checkInitialization()
        return retrofit
    }

    /**
     * 检查是否初始化
     */
    private fun checkInitialization() {
        if (!isInitialized) {
            throw IllegalStateException("NetworkFramework not initialized. Call init() first.")
        }
    }

    /**
     * 构建Multipart请求体
     */
    private fun buildMultipartBody(file: File, params: Map<String, Any>): RequestBody {
        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        // 添加文本参数
        params.forEach { (key, value) ->
            builder.addFormDataPart(key, value.toString())
        }

        // 添加文件
        val fileBody = RequestBody.create(
            (getMimeType(file) ?: "application/octet-stream").toMediaTypeOrNull(),
            file
        )
        builder.addFormDataPart("file", file.name, fileBody)

        return builder.build()
    }

    /**
     * 获取文件的MIME类型
     */
    private fun getMimeType(file: File): String? {
        return when (file.extension.toLowerCase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            else -> null
        }
    }

    /**
     * 清除网络缓存
     */
    fun clearCache() {
        try {
            okHttpClient.cache?.evictAll()
            Logger.i("Network cache cleared successfully")
        } catch (e: Exception) {
            Logger.e("Failed to clear network cache: ${e.message}")
        }
    }

    /**
     * 取消所有网络请求
     */
    fun cancelAllRequests() {
        okHttpClient.dispatcher.cancelAll()
        Logger.i("All network requests cancelled")
    }
}