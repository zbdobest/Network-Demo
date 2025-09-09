package com.example.http.http

import android.content.Context
import android.os.Environment
import com.example.http.http.callback.DownloadCallback
import com.example.http.http.callback.NetworkCallback
import com.example.http.http.callback.UploadCallback
import com.example.http.http.constants.HttpConstants
import com.example.http.http.dsl.NetworkDownloadDSL
import com.example.http.http.dsl.NetworkFileUploadDSL
import com.example.http.http.dsl.NetworkRequestDSL
import com.example.http.http.framework.HttpManager
import com.example.http.http.framework.NetworkConfig
import com.example.http.http.framework.RequestBuilder
import com.example.http.http.model.BaseResponse
import com.example.http.http.model.Progress
import com.example.http.http.utils.Logger
import com.example.http.http.utils.NetworkUtils
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.QueryMap
import retrofit2.http.Url
import java.io.File

/**
 * 网络管理器
 * 封装了常见的网络操作
 */
object NetworkManager {

    private lateinit var networkFramework: HttpManager
    private val requestBuilder = RequestBuilder()

    /**
     * 初始化网络管理器
     */
    fun init(config: NetworkConfig) {
        HttpManager.init(config)
        networkFramework = HttpManager
        Logger.i("NetworkManager initialized")
    }

    // region 普通网络请求

    /**
     * 执行GET请求
     * @param url 请求地址
     * @param callback 回调接口
     */
    fun <T> get(url: String, callback: NetworkCallback<T>, clazz: Class<T>) {
        executeRequest("GET", url, null, callback,clazz)
    }

    /**
     * 执行POST请求
     * @param url 请求地址
     * @param body 请求体
     * @param callback 回调接口
     */
    fun <T> post(url: String, body: Any? = null, callback: NetworkCallback<T>,clazz: Class<T>) {
        executeRequest("POST", url, body, callback,clazz)
    }

    /**
     * 执行PUT请求
     * @param url 请求地址
     * @param body 请求体
     * @param callback 回调接口
     */
    fun <T> put(url: String, body: Any? = null, callback: NetworkCallback<T>,clazz: Class<T>) {
        executeRequest("PUT", url, body, callback,clazz)
    }

    /**
     * 执行DELETE请求
     * @param url 请求地址
     * @param callback 回调接口
     */
    fun <T> delete(url: String, callback: NetworkCallback<T>,clazz: Class<T>) {
        executeRequest("DELETE", url, null, callback,clazz)
    }

    /**
     * 使用API服务执行请求（推荐方式）
     * @param call Retrofit Call对象
     * @param callback 回调接口
     */
    fun <T> execute(call: Call<BaseResponse<T>>, callback: NetworkCallback<T>) {
        networkFramework.execute(call, callback)
    }

    /**
     * 使用RxJava执行请求
     * @param observable RxJava Observable
     * @param callback 回调接口
     */
    fun <T> executeRx(observable: Observable<BaseResponse<T>>, callback: NetworkCallback<T>) {
        networkFramework.executeRx(observable, callback)
    }

    // endregion

    // region 文件操作

    /**
     * 上传单个文件
     * @param url 上传地址
     * @param file 要上传的文件
     * @param fileKey 文件字段名（默认为"file"）
     * @param params 附加参数
     * @param callback 上传回调
     */
    fun uploadFile(
        url: String,
        file: File,
        fileKey: String = "file",
        params: Map<String,@JvmSuppressWildcards Any> = emptyMap(),
        callback: UploadCallback
    ) {
        val uploadParams = params.toMutableMap()
        uploadParams[fileKey] = file

        networkFramework.upload(url, file, uploadParams, requestBuilder.buildHeaders(), callback)
    }

    /**
     * 上传多个文件
     * @param url 上传地址
     * @param files 文件列表
     * @param fileKey 文件字段名
     * @param params 附加参数
     * @param callback 上传回调
     */
    fun uploadFiles(
        url: String,
        files: List<File>,
        fileKey: String = "files",
        params: Map<String,@JvmSuppressWildcards Any> = emptyMap(),
        callback: UploadCallback
    ) {
        // 这里简化处理，实际应该支持多文件上传
        if (files.isNotEmpty()) {
            uploadFile(url, files.first(), fileKey, params, callback)
        }
    }

    /**
     * 下载文件
     * @param url 下载地址
     * @param fileName 文件名
     * @param directory 下载目录
     * @param callback 下载回调
     */
    fun downloadFile(
        url: String,
        fileName: String,
        directory: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        callback: DownloadCallback
    ) {
        val destination = File(directory, fileName)
        networkFramework.download(url, destination, requestBuilder.buildHeaders(), callback)
    }

    // endregion

    // region 请求构建器方法

    /**
     * 添加请求头
     */
    fun withHeader(key: String, value: String): NetworkManager {
        requestBuilder.addHeader(key, value)
        return this
    }

    /**
     * 添加多个请求头
     */
    fun withHeaders(headers: Map<String, String>): NetworkManager {
        requestBuilder.addHeaders(headers)
        return this
    }

    /**
     * 添加请求参数
     */
    fun withParam(key: String, value: Any): NetworkManager {
        requestBuilder.addParam(key, value)
        return this
    }

    /**
     * 添加多个请求参数
     */
    fun withParams(params: Map<String,@JvmSuppressWildcards Any>): NetworkManager {
        requestBuilder.addParams(params)
        return this
    }

    /**
     * 清除所有请求参数和头
     */
    fun clearRequest() {
        requestBuilder.clear()
    }

    // endregion

    // region 工具方法

    /**
     * 创建API服务
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return networkFramework.createService(serviceClass)
    }

    /**
     * 取消所有网络请求
     */
    fun cancelAll() {
        networkFramework.cancelAllRequests()
    }

    /**
     * 清除网络缓存
     */
    fun clearCache() {
        networkFramework.clearCache()
    }

    /**
     * 检查网络是否可用
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return NetworkUtils.isNetworkAvailable(context)
    }

    // endregion

    // region 私有方法

    /**
     * 执行网络请求（内部实现）
     */
    private fun <T> executeRequest(
        method: String,
        url: String,
        body: Any?,
        callback: NetworkCallback<T>,
        clazz: Class<T>
    ) {
        // 简化实现，实际应该根据method和参数构建请求
        Logger.d("Executing $method request to $url")
        val service = createService(DynamicApiService::class.java)
        val call = when (method) {
            "GET" -> service.dynamicGet<T>(url, requestBuilder.buildParams())
            "POST" -> service.dynamicPost<T>(url, body ?: requestBuilder.buildParams())
            "PUT" -> service.dynamicPut<T>(url, body ?: requestBuilder.buildParams())
            "DELETE" -> service.dynamicDelete<T>(url, requestBuilder.buildParams())
            else -> throw IllegalArgumentException("Unsupported HTTP method: $method")
        }
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val json = response.body()?.string() ?: return
                    val baseResponse = Gson().fromJson(json, BaseResponse::class.java)
                    if (baseResponse.error_code == HttpConstants.HTTP_RESPONSE_CODE_SUCCESS) {
                        val data = Gson().fromJson(
                            Gson().toJson(baseResponse.result),
                            clazz
                        )
                        callback.onSuccess(data as T)
                    } else {
                        callback.onError(baseResponse.error_code, baseResponse.reason)
                    }
                } else {
                    callback.onError(response.code(), response.message())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onException(t)
            }
        })
    }

    // endregion
}

/**
 * 动态API服务接口（用于支持任意URL的请求）
 */
interface DynamicApiService {
    @GET
    fun <T> dynamicGet(
        @Url url: String,
        @QueryMap params: Map<String,@JvmSuppressWildcards Any>
    ): Call<ResponseBody>

    @POST
    fun <T> dynamicPost(
        @Url url: String,
        @Body body: Any
    ): Call<ResponseBody>

    @PUT
    fun <T> dynamicPut(
        @Url url: String,
        @Body body: Any
    ): Call<ResponseBody>

    @DELETE
    fun <T> dynamicDelete(
        @Url url: String,
        @QueryMap params: Map<String,@JvmSuppressWildcards Any>
    ): Call<ResponseBody>
}

/**
 * NetworkManager的扩展函数
 */

// GET请求扩展
inline fun <reified T> NetworkManager.getWithLambda(
    url: String,
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onError: (Int, String) -> Unit = { _, _ -> },
    crossinline onException: (Throwable) -> Unit = {}
) {
    get(url, object : NetworkCallback<T> {
        override fun onSuccess(data: T) = onSuccess(data)
        override fun onError(code: Int, message: String) = onError(code, message)
        override fun onException(throwable: Throwable) = onException(throwable)
    }, clazz = T::class.java)
}

// POST请求扩展
inline fun <reified T> NetworkManager.postWithLambda(
    url: String,
    body: Any? = null,
    crossinline onSuccess: (T) -> Unit = {},
    crossinline onError: (Int, String) -> Unit = { _, _ -> },
    crossinline onException: (Throwable) -> Unit = {}
) {
    post(url, body, object : NetworkCallback<T> {
        override fun onSuccess(data: T) = onSuccess(data)
        override fun onError(code: Int, message: String) = onError(code, message)
        override fun onException(throwable: Throwable) = onException(throwable)
    }, clazz = T::class.java)
}

// 上传文件扩展
inline fun NetworkManager.uploadFileWithLambda(
    url: String,
    file: File,
    fileKey: String = "file",
    params: Map<String,@JvmSuppressWildcards Any> = emptyMap(),
    crossinline onProgress: (Progress) -> Unit = {},
    crossinline onSuccess: (String) -> Unit = {},
    crossinline onError: (Int, String) -> Unit = { _, _ -> },
    crossinline onException: (Throwable) -> Unit = {}
) {
    uploadFile(url, file, fileKey, params, object : UploadCallback {
        override fun onProgress(progress: Progress) = onProgress(progress)
        override fun onSuccess(url: String) = onSuccess(url)
        override fun onError(code: Int, message: String) = onError(code, message)
        override fun onException(throwable: Throwable) = onException(throwable)
    })
}

// 下载文件扩展
inline fun NetworkManager.downloadFileWithLambda(
    url: String,
    fileName: String,
    directory: File,
    params: Map<String,@JvmSuppressWildcards Any> = emptyMap(),
    crossinline onProgress: (Progress) -> Unit = {},
    crossinline onSuccess: (File) -> Unit = {},
    crossinline onError: (Int, String) -> Unit = { _, _ -> },
    crossinline onException: (Throwable) -> Unit = {}
) {
    downloadFile(url, fileName, directory, object : DownloadCallback {
        override fun onProgress(progress: Progress) = onProgress(progress)
        override fun onSuccess(file: File) = onSuccess(file)
        override fun onError(code: Int, message: String) = onError(code, message)
        override fun onException(throwable: Throwable) = onException(throwable)
    })
}

/**
 * DSL扩展函数
 */
// GET请求扩展
inline fun <reified T> NetworkManager.getWithDSL(
    url: String,
    crossinline init: NetworkRequestDSL<T>.() -> Unit
) {
    val dsl = NetworkRequestDSL<T>().apply(init)
    get(url, object : NetworkCallback<T> {
        override fun onSuccess(data: T) {
            dsl.onSuccess?.invoke(data)
        }

        override fun onError(code: Int, message: String) {
            dsl.onError?.invoke(code, message)
        }

        override fun onException(throwable: Throwable) {
            dsl.onException?.invoke(throwable)
        }
    }, clazz = T::class.java)
}

// POST请求扩展
inline fun <reified T> NetworkManager.postWithSDL(
    url: String,
    body: Any? = null,
    crossinline init: NetworkRequestDSL<T>.() -> Unit
) {
    post(url, body, object : NetworkCallback<T> {
        val dsl = NetworkRequestDSL<T>().apply(init)
        override fun onSuccess(data: T) {
            dsl.onSuccess?.invoke(data)
        }

        override fun onError(code: Int, message: String) {
            dsl.onError?.invoke(code, message)
        }

        override fun onException(throwable: Throwable) {
            dsl.onException?.invoke(throwable)
        }
    }, clazz = T::class.java)
}

// 上传文件扩展
inline fun NetworkManager.uploadFileWithDSL(
    url: String,
    file: File,
    fileKey: String = "file",
    params: Map<String,@JvmSuppressWildcards Any> = emptyMap(),
    crossinline init: NetworkFileUploadDSL.() -> Unit
) {
    val dsl = NetworkFileUploadDSL().apply(init)
    uploadFile(url, file, fileKey, params, object : UploadCallback {
        override fun onProgress(progress: Progress) {
            dsl.onProgress?.invoke(progress)
        }

        override fun onSuccess(url: String) {
            dsl.onSuccess?.invoke(url)
        }

        override fun onError(code: Int, message: String) {
            dsl.onError?.invoke(code, message)
        }

        override fun onException(throwable: Throwable) {
            dsl.onException?.invoke(throwable)
        }
    })
}

// 下载文件扩展
inline fun NetworkManager.downloadFileWithLambda(
    url: String,
    fileName: String,
    directory: File,
    params: Map<String, @JvmSuppressWildcards Any> = emptyMap(),
    crossinline init: NetworkDownloadDSL.() -> Unit
) {
    val dsl = NetworkDownloadDSL().apply(init)
    downloadFile(url, fileName, directory, object : DownloadCallback {
        override fun onProgress(progress: Progress) {
            dsl.onProgress?.invoke(progress)
        }

        override fun onSuccess(file: File) {
            dsl.onSuccess?.invoke(file)
        }

        override fun onError(code: Int, message: String) {
            dsl.onError?.invoke(code, message)
        }

        override fun onException(throwable: Throwable) {
            dsl.onException?.invoke(throwable)
        }
    })
}