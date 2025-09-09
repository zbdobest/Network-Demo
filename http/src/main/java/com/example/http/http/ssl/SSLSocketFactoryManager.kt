package com.example.http.http.ssl

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object SSLSocketFactoryManager {

    fun createUnsafeSSLContext(): SSLContext {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            })

            SSLContext.getInstance("TLS").apply {
                init(null, trustAllCerts, SecureRandom())
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to create SSL context", e)
        }
    }

    fun createTrustAllHostnameVerifier(): HostnameVerifier {
        return HostnameVerifier { _, _ -> true }
    }

    fun applyUnsafeSSLSettings(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder
            .sslSocketFactory(createUnsafeSSLContext().socketFactory, createTrustAllTrustManager())
            .hostnameVerifier(createTrustAllHostnameVerifier())
            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
    }

    private fun createTrustAllTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
    }
}