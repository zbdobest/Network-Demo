# Network-HTTP Library

一个基于Kotlin开发的高扩展性、功能完善的HTTP网络库，集成了DSL简化调用、多类型拦截器、统一回调管理、通用错误处理、SSL安全校验和进度监控等功能。

## 📁 项目结构详解

```
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
│   ├── Logger.kt             # 日志工具
│   └── NetworkUtils.kt       # 网络工具
└── NetworkManager.kt     # 网络状态管理
```

## 🌟 核心设计理念

### 1. 分层架构设计
```
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
```

## 🛡️ 核心组件封装

### 1. 拦截器机制

**设计思路**：通过责任链模式实现功能解耦

```kotlin
// 公共参数拦截器
class CommonParamsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("version", "1.0")
            .addQueryParameter("platform", "android")
            .build()
        return chain.proceed(originalRequest.newBuilder().url(newUrl).build())
    }
}

// 公共头拦截器  
class CommonHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer token")
            .addHeader("User-Agent", "MyApp/1.0")
            .build()
        return chain.proceed(request)
    }
}

// 日志拦截器（Debug模式启用）
if (isDebug) {
    clientBuilder.addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
}
```

### 2. 回调系统

**设计思路**：统一回调接口 + 线程安全处理

```kotlin
// 基础回调接口
interface NetworkCallback<T> {
    fun onSuccess(data: T)              // 成功回调
    fun onError(code: Int, message: String)  // 业务错误
    fun onException(throwable: Throwable)    // 网络异常
}

// 线程安全处理
private fun executeOnMainThread(action: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        action()
    } else {
        Handler(Looper.getMainLooper()).post(action)
    }
}
```

### 3. 通用错误处理

**设计思路**：集中式错误码管理 + 统一转换

```kotlin
object ErrorCodeHandler {
    private val errorCodeMap = mutableMapOf<Int, String>().apply {
        put(400, "请求参数错误")
        put(401, "未授权，请重新登录")
        put(404, "请求的资源不存在")
        put(500, "服务器内部错误")
    }
    
    fun handleError(code: Int, message: String): String {
        return errorCodeMap[code] ?: message
    }
    
    // 统一错误处理
    fun <T> handleResponse(response: BaseResponse<T>, callback: NetworkCallback<T>) {
        if (response.code == 200) {
            response.data?.let(callback::onSuccess)
        } else {
            val errorMsg = getErrorMessage(response.code)
            callback.onError(response.code, errorMsg)
        }
    }
}
```

### 4. SSL证书校验

**设计思路**：支持自定义证书 + 默认系统信任

```kotlin
// 自定义SSL配置
fun initSSL(context: Context) {
    val trustManager = createTrustManager(context)
    val sslSocketFactory = createSSLSocketFactory(trustManager)
    
    NetworkConfig.create()
        .sslSocketFactory(sslSocketFactory, trustManager)
}

// 默认使用系统证书
val client = OkHttpClient.Builder()
    .sslSocketFactory(platform.socketFactory, platform.trustManager)
    .build()
```

## 📱 使用方法

### 基础数据请求

```kotlin
// 1. GET请求 - 查询用户信息
NetworkManager.getWithLambda<UserInfo>("user/info",
    onSuccess = { user -> 
        // 处理用户数据
        updateUI(user)
    },
    onError = { code, message ->
        // 统一错误处理
        showError(ErrorCodeHandler.handleError(code, message))
    }
)

// 2. POST请求 - 提交数据
val userData = User(name = "John", age = 25)
NetworkManager.postWithLambda<ApiResponse>("user/update", userData,
    onSuccess = { response ->
        showToast("更新成功")
    }
)

// 3. 链式调用配置
NetworkManager
    .withHeader("Authorization", "user_token")
    .withParam("timestamp", System.currentTimeMillis())
    .getWithLambda<ProductList>("products")
```

### 文件上传

```kotlin
// 单文件上传
NetworkManager.uploadFileWithLambda(
    url = "https://api.example.com/upload",
    file = File("/sdcard/image.jpg"),
    fileKey = "avatar",  // 表单字段名
    params = mapOf("description" to "用户头像"),
    onProgress = { progress ->
        // 实时更新进度条
        val percent = progress.progress
        updateProgress(percent)
    },
    onSuccess = { fileUrl ->
        // 上传成功，获取文件访问URL
        showMessage("上传成功: $fileUrl")
    }
)

// 多文件上传（循环调用单文件上传）
val files = listOf(File("1.jpg"), File("2.jpg"), File("3.jpg"))
files.forEach { file ->
    NetworkManager.uploadFileWithLambda("upload", file, "images")
}
```

### 文件下载

```kotlin
// 文件下载到指定目录
NetworkManager.downloadFileWithLambda(
    url = "https://example.com/file.pdf",
    fileName = "document.pdf", 
    directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
    onProgress = { progress ->
        // 显示下载进度
        val speed = NetworkUtils.calculateSpeed(progress.currentBytes, progress.elapsedTime)
        showDownloadProgress(progress.progress, speed)
    },
    onSuccess = { file ->
        // 下载完成，打开文件
        openFile(file)
    },
    onError = { code, message ->
        showError("下载失败: $message")
    }
)
```

### 高级功能使用

```kotlin
// 1. 自定义拦截器（添加签名验证）
class SignatureInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val signedRequest = original.newBuilder()
            .addHeader("Signature", generateSignature(original))
            .build()
        return chain.proceed(signedRequest)
    }
}

// 2. 自定义SSL证书（内嵌证书）
val certInputStream = context.resources.openRawResource(R.raw.my_cert)
val sslContext = SSLContext.getInstance("TLS")
sslContext.init(null, createTrustManager(certInputStream), null)

NetworkConfig.create()
    .sslSocketFactory(sslContext.socketFactory, trustManager)

// 3. 请求取消管理
// 在Activity/Fragment销毁时取消请求
override fun onDestroy() {
    super.onDestroy()
    NetworkManager.cancelAll()  // 取消所有请求
}
```

## 🔧 配置选项

### 网络配置示例
```kotlin
val config = NetworkConfig.create()
    .baseUrl("https://api.example.com/")          // 基础URL
    .connectTimeout(15)                           // 连接超时(秒)
    .readTimeout(20)                              // 读取超时(秒)
    .writeTimeout(20)                             // 写入超时(秒)
    .commonParams(mapOf(                          // 公共参数
        "appVersion" to BuildConfig.VERSION_NAME,
        "platform" to "android"
    ))
    .commonHeaders(mapOf(                         // 公共头
        "Authorization" to "Bearer token",
        "Content-Type" to "application/json"
    ))
    .addInterceptor(MyCustomInterceptor())        // 自定义拦截器
    .debug(BuildConfig.DEBUG)                     // 调试模式

NetworkManager.init(config)
```

## 🎯 设计优势

1. **解耦设计**：各组件职责单一，易于维护和扩展
2. **线程安全**：自动主线程回调，避免UI线程问题
3. **类型安全**：泛型支持，编译时类型检查
4. **灵活配置**：支持多种配置方式和自定义扩展
5. **统一管理**：集中错误处理、日志记录和资源管理
6. **性能优化**：连接池复用、缓存策略等内置优化

这个框架提供了从简单请求到复杂文件传输的完整解决方案，具有良好的可扩展性和易用性。
