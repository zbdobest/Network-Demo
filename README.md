Network-HTTP Library
一个基于Kotlin开发的高扩展性、功能完善的HTTP网络库，集成了DSL简化调用、多类型拦截器、统一回调管理、通用错误处理、SSL安全校验和进度监控等功能。

📁 项目结构详解
text
java/com/example/http/http/
├── callback/                 # 回调接口定义
│   ├── DownloadCallback.kt   # 下载专用回调（含进度）
│   ├── NetworkCallback.kt    # 通用网络回调
│   ├── ProgressListener.kt   # 进度监听接口
│   └── UploadCallback.kt     # 上传专用回调（含进度）
├── constants/
│   └── HttpConstants.kt      # HTTP常量定义
├── dsl/                      # DSL扩展模块
│   ├── NetworkDownloadDSL.kt # 下载DSL语法
│   ├── NetworkFileUploadDSL.kt # 上传DSL语法
│   └── NetworkRequestDSL.kt  # 通用请求DSL语法
├── error/
│   └── ErrorCodeHandler.kt   # 错误码统一处理
├── framework/                # 核心框架
│   ├── HttpManager.kt        # 网络管理核心类
│   ├── NetworkConfig.kt      # 网络配置管理
│   ├── NetworkRequest.kt     # 网络请求抽象
│   └── RequestBuilder.kt     # 请求建造者
├── interceptor/              # 拦截器模块
│   ├── CommonHeadersInterceptor.kt # 通用头拦截器
│   ├── CommonParamsInterceptor.kt  # 通用参数拦截器
│   ├── ProgressInterceptor.kt      # 进度拦截器
│   └── ProgressResponseBody.kt     # 进度响应体包装
├── model/                    # 数据模型
│   ├── BaseResponse.kt       # 响应基类
│   ├── HttpResult.kt         # 请求结果封装
│   └── Progress.kt           # 进度数据模型
├── ssl/
│   └── SSLSocketFactoryManager.kt # SSL安全管理
└── utils/                    # 工具类
    ├── Logger.kt             # 日志工具
    ├── NetworkUtils.kt       # 网络工具
    └── NetworkManager.kt     # 网络状态管理
🌟 核心设计理念
1. 分层架构设计
text
┌─────────────────────────┐
│   DSL层 (简化调用)        │
├─────────────────────────┤
│   业务层 (回调/错误处理)   │
├─────────────────────────┤
│   拦截器层 (功能扩展)      │
├─────────────────────────┤
│   核心框架层 (请求管理)    │
├─────────────────────────┤
│   安全层 (SSL校验)        │
└─────────────────────────┘
🔧 核心特性详解
1. 强大的拦截器系统
通用头拦截器
kotlin
class CommonHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("User-Agent", "Network-HTTP/1.0")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
通用参数拦截器
kotlin
class CommonParamsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("device_id", getDeviceId())
            .addQueryParameter("version", appVersion)
            .build()
        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
进度监控拦截器
kotlin
class ProgressInterceptor(private val listener: ProgressListener) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
            .body(ProgressResponseBody(originalResponse.body!!, listener))
            .build()
    }
}
2. 统一的回调管理
通用网络回调
kotlin
interface NetworkCallback<T> {
    fun onSuccess(result: HttpResult<T>)
    fun onFailure(error: Throwable)
    fun onProgress(progress: Progress) // 进度回调
}

// 下载专用回调
interface DownloadCallback : NetworkCallback<File> {
    fun onDownloadStarted()
    fun onDownloadCompleted(file: File)
}
3. 完善的错误处理
kotlin
object ErrorCodeHandler {
    fun handleError(error: Throwable): String {
        return when (error) {
            is SocketTimeoutException -> "连接超时，请检查网络"
            is ConnectException -> "网络连接失败"
            is SSLHandshakeException -> "证书验证失败"
            is HttpException -> handleHttpError(error.code())
            else -> "网络请求失败: ${error.message}"
        }
    }
    
    private fun handleHttpError(code: Int): String {
        return when (code) {
            401 -> "未授权，请重新登录"
            403 -> "访问被拒绝"
            404 -> "请求的资源不存在"
            500 -> "服务器内部错误"
            else -> "HTTP错误: $code"
        }
    }
}
4. SSL安全校验
kotlin
class SSLSocketFactoryManager {
    companion object {
        fun createSSLSocketFactory(): SSLSocketFactory {
            val trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    // 这里可以实现证书锁定(Pinning)逻辑
                    chain?.forEach { certificate ->
                        // 验证证书指纹等
                    }
                }
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
            
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), SecureRandom())
            return sslContext.socketFactory
        }
    }
}
5. DSL简化调用
网络请求DSL
kotlin
networkRequest {
    url = "https://api.example.com/users"
    method = Method.GET
    headers {
        "Authorization" to "Bearer token123"
        "Content-Type" to "application/json"
    }
    params {
        "page" to "1"
        "limit" to "20"
    }
    onSuccess { result: HttpResult<User> ->
        // 处理成功结果
    }
    onFailure { error ->
        // 处理错误
    }
    onProgress { progress ->
        // 更新进度
    }
}
文件上传DSL
kotlin
networkUpload {
    url = "https://api.example.com/upload"
    file = File("/path/to/file.jpg")
    fileFieldName = "avatar"
    onSuccess { result ->
        println("上传成功: ${result.data}")
    }
    onProgress { progress ->
        updateProgressBar(progress.percentage)
    }
}
文件下载DSL
kotlin
networkDownload {
    url = "https://example.com/large-file.zip"
    destination = File("/downloads/large-file.zip")
    onDownloadStarted {
        showDownloadNotification()
    }
    onProgress { progress ->
        updateNotification(progress.percentage)
    }
    onDownloadCompleted { file ->
        completeNotification(file)
    }
}
🚀 使用方法
1. 初始化配置
kotlin
// 在Application中初始化
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        NetworkConfig.initialize {
            baseUrl = "https://api.example.com"
            connectTimeout = 30_000
            readTimeout = 30_000
            addInterceptor(CommonHeadersInterceptor())
            addInterceptor(CommonParamsInterceptor())
            sslSocketFactory = SSLSocketFactoryManager.createSSLSocketFactory()
            debugMode = BuildConfig.DEBUG
        }
    }
}
2. 基础数据请求
kotlin
// 使用DSL方式
networkRequest {
    url = "/user/profile"
    method = Method.GET
    onSuccess { result: HttpResult<UserProfile> ->
        // 处理用户资料
        val user = result.data
        updateUI(user)
    }
    onFailure { error ->
        showToast(ErrorCodeHandler.handleError(error))
    }
}

// 使用传统方式
val request = RequestBuilder()
    .url("https://api.example.com/posts")
    .method(Method.POST)
    .body(createJsonBody(postData))
    .build()

HttpManager.execute(request, object : NetworkCallback<List<Post>> {
    override fun onSuccess(result: HttpResult<List<Post>>) {
        // 处理成功
    }
    
    override fun onFailure(error: Throwable) {
        // 处理错误
    }
    
    override fun onProgress(progress: Progress) {
        // 进度更新
    }
})
3. 文件上传
kotlin
networkUpload {
    url = "/upload/avatar"
    file = avatarFile
    fileFieldName = "avatar_image"
    addFormData("user_id", "12345")
    onProgress { progress ->
        binding.progressBar.progress = progress.percentage
        binding.percentageText.text = "${progress.percentage}%"
    }
    onSuccess { result ->
        showToast("头像上传成功")
    }
    onFailure { error ->
        showToast("上传失败: ${error.message}")
    }
}
4. 文件下载
kotkin
networkDownload {
    url = "https://example.com/large-video.mp4"
    destination = File(context.getExternalFilesDir(null), "video.mp4")
    onDownloadStarted {
        showDownloadStarted("开始下载视频")
    }
    onProgress { progress ->
        updateDownloadProgress(progress.downloaded, progress.total)
    }
    onDownloadCompleted { file ->
        openVideoFile(file)
    }
    onFailure { error ->
        showDownloadError("下载失败")
    }
}
🛠 高级配置
自定义拦截器
kotlin
// 添加日志拦截器
class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Logger.d("Request: ${request.url} ${request.headers}")
        
        val response = chain.proceed(request)
        Logger.d("Response: ${response.code} ${response.message}")
        
        return response
    }
}

// 注册到配置中
NetworkConfig.addInterceptor(LoggingInterceptor())
自定义SSL证书校验
kotlin
// 实现自定义证书验证逻辑
val customTrustManager = object : X509TrustManager {
    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        // 实现证书锁定或自定义验证逻辑
        if (!isCertificateTrusted(chain[0])) {
            throw SSLHandshakeException("证书验证失败")
        }
    }
    // 其他方法实现...
}
📊 性能特性
连接池管理: 内置HTTP连接池，减少连接建立开销

请求复用: 支持请求取消和复用

内存优化: 使用OkIO进行流处理，减少内存占用

进度监控: 精确的上传下载进度反馈

线程安全: 所有公共方法都保证线程安全
