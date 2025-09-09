package com.example.http.http.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL
import java.util.Collections

/**
 * 网络工具类 - 提供网络相关的实用方法
 */
object NetworkUtils {

    /**
     * 检查网络是否可用
     * @param context 上下文
     * @return 网络是否可用
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        } catch (e: Exception) {
            Logger.e("Network availability check failed: ${e.message}")
            false
        }
    }

    /**
     * 获取当前网络类型
     * @param context 上下文
     * @return 网络类型字符串 (WIFI, MOBILE, ETHERNET, UNKNOWN)
     */
    fun getNetworkType(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WIFI"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "MOBILE"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "ETHERNET"
                else -> "UNKNOWN"
            }
        } catch (e: Exception) {
            Logger.e("Get network type failed: ${e.message}")
            "UNKNOWN"
        }
    }

    /**
     * 检查是否是WiFi网络
     * @param context 上下文
     * @return 是否是WiFi
     */
    fun isWifiConnected(context: Context): Boolean {
        return getNetworkType(context) == "WIFI"
    }

    /**
     * 检查是否是移动数据网络
     * @param context 上下文
     * @return 是否是移动数据
     */
    fun isMobileDataConnected(context: Context): Boolean {
        return getNetworkType(context) == "MOBILE"
    }

    /**
     * 获取网络信号强度（仅适用于Android 10+）
     * @param context 上下文
     * @return 信号强度 (-1表示未知)
     */
    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getSignalStrength(context: Context): Int {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            capabilities?.signalStrength ?: -1
        } catch (e: Exception) {
            Logger.e("Get signal strength failed: ${e.message}")
            -1
        }
    }

    /**
     * 获取设备IP地址
     * @return IP地址字符串
     */
    fun getLocalIpAddress(): String {
        return try {
            val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in networkInterfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "Unknown"
                    }
                }
            }
            "Unknown"
        } catch (e: Exception) {
            Logger.e("Get local IP address failed: ${e.message}")
            "Unknown"
        }
    }

    /**
     * 获取公网IP地址（异步）
     * @param callback IP地址回调
     */
    fun getPublicIpAddress(callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://api.ipify.org")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val ipAddress = reader.readLine()
                reader.close()

                withContext(Dispatchers.Main) {
                    callback(ipAddress)
                }
            } catch (e: Exception) {
                Logger.e("Get public IP address failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback("Unknown")
                }
            }
        }
    }

    /**
     * 检查URL是否可达
     * @param urlString URL字符串
     * @param timeout 超时时间（毫秒）
     * @return 是否可达
     */
    fun isUrlReachable(urlString: String, timeout: Int = 5000): Boolean {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = timeout
            connection.readTimeout = timeout
            connection.requestMethod = "HEAD"
            connection.connect()
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            Logger.e("URL reachability check failed: ${e.message}")
            false
        }
    }

    /**
     * 获取网络速度（近似值）
     * @param context 上下文
     * @return 网络速度字符串 (SLOW, NORMAL, FAST)
     */
    fun getNetworkSpeed(context: Context): String {
        return if (!isNetworkAvailable(context)) {
            "NO_NETWORK"
        } else {
            when (getNetworkType(context)) {
                "WIFI" -> "FAST"
                "MOBILE" -> {
                    // 这里可以进一步区分4G/5G等
                    "NORMAL"
                }
                "ETHERNET" -> "FAST"
                else -> "SLOW"
            }
        }
    }

    /**
     * 格式化文件大小
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    /**
     * 计算下载/上传速度
     * @param bytesTransferred 已传输字节数
     * @param timeElapsed 经过的时间（秒）
     * @return 速度字符串
     */
    fun calculateSpeed(bytesTransferred: Long, timeElapsed: Long): String {
        if (timeElapsed == 0L) return "0 B/s"

        val bytesPerSecond = bytesTransferred / timeElapsed
        return "${formatFileSize(bytesPerSecond)}/s"
    }

    /**
     * 获取MIME类型根据文件扩展名
     * @param file 文件
     * @return MIME类型
     */
    fun getMimeType(file: File): String {
        return when (file.extension.toLowerCase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "txt" -> "text/plain"
            "html", "htm" -> "text/html"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            else -> "application/octet-stream"
        }
    }

    /**
     * 验证URL格式
     * @param url URL字符串
     * @return 是否有效
     */
    fun isValidUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    /**
     * 获取域名从URL
     * @param url URL字符串
     * @return 域名
     */
    fun getDomainFromUrl(url: String): String {
        return try {
            val uri = Uri.parse(url)
            uri.host ?: "Invalid URL"
        } catch (e: Exception) {
            Logger.e("Get domain from URL failed: ${e.message}")
            "Invalid URL"
        }
    }

    /**
     * 创建安全的文件名（移除非法字符）
     * @param fileName 原文件名
     * @return 安全的文件名
     */
    fun createSafeFileName(fileName: String): String {
        return fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
    }
}