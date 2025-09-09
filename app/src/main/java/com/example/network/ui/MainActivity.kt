package com.example.network.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.http.http.NetworkManager
import com.example.http.http.callback.NetworkCallback
import com.example.http.http.getWithLambda
import com.example.http.http.postWithSDL
import com.example.network.R
import com.example.network.api.ApiService
import com.example.network.bean.HuangliResponse
import com.example.network.bean.NewsResponse

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.test_request_get).setOnClickListener {
            requestData()
        }
        findViewById<TextView>(R.id.test_request_post).setOnClickListener {
            requestPost()
        }
        findViewById<TextView>(R.id.test_request_get_url).setOnClickListener {
            requestGetWidthUrl()
        }
        findViewById<TextView>(R.id.test_request_get_lambda).setOnClickListener {
            requestGetWidthLambda()
        }
        findViewById<TextView>(R.id.test_request_post_dsl).setOnClickListener {
            requestPostWidthDSL()
        }
    }

    private fun requestData() {
        val service = NetworkManager.createService(ApiService::class.java)
        NetworkManager.execute(
            service.toutiaoContent(
                "93fa38f3b6b15c852b543c5bfb628cf0",
                "ef4480ce866214af80bb9e77b78e4a5c"
            ), object : NetworkCallback<NewsResponse> {
                override fun onSuccess(data: NewsResponse) {
                    Toast.makeText(this@MainActivity, data.toString(), Toast.LENGTH_LONG)
                        .show()
                }

                override fun onError(code: Int, message: String) {
                }

                override fun onException(throwable: Throwable) {
                }

            })


    }

    private fun requestPost() {
        val service = NetworkManager.createService(ApiService::class.java)
        NetworkManager.execute(
            service.laohuangli(
                "7bac7d66c2b37f23a087f5b86c3b373f",
                "2014-09-09"
            ), object : NetworkCallback<HuangliResponse> {
                override fun onSuccess(data: HuangliResponse) {
                    Toast.makeText(this@MainActivity, data.toString(), Toast.LENGTH_LONG)
                        .show()
                }

                override fun onError(code: Int, message: String) {
                }

                override fun onException(throwable: Throwable) {
                }
            })
    }

    private fun requestGetWidthUrl() {
        NetworkManager
            .withParams(
                mapOf(
                    "uniquekey" to "93fa38f3b6b15c852b543c5bfb628cf0",
                    "key" to "ef4480ce866214af80bb9e77b78e4a5c"
                )
            )
            .get("toutiao/content", object : NetworkCallback<NewsResponse> {
                override fun onSuccess(data: NewsResponse) {
                    Toast.makeText(this@MainActivity, data.toString(), Toast.LENGTH_LONG)
                        .show()
                }

                override fun onError(code: Int, message: String) {
                }

                override fun onException(throwable: Throwable) {
                }
            }, NewsResponse::class.java)

    }

    private fun requestGetWidthLambda() {
        NetworkManager
            .withParams(
                mapOf(
                    "uniquekey" to "93fa38f3b6b15c852b543c5bfb628cf0",
                    "key" to "ef4480ce866214af80bb9e77b78e4a5c"
                )
            )
            .getWithLambda<NewsResponse>("toutiao/content",
                onSuccess = { data ->
                    Toast.makeText(this@MainActivity, data.toString(), Toast.LENGTH_LONG)
                        .show()
                },
                onError = { code, msg ->

                },

                onException = { throwable ->

                }

            )

    }

    private fun requestPostWidthDSL() {
        NetworkManager.withParams(
            mapOf(
                "uniquekey" to "7bac7d66c2b37f23a087f5b86c3b373f",
                "date" to "2014-09-09"
            )
        ).postWithSDL<HuangliResponse>("", "", {
            onSuccess = { data ->
                Toast.makeText(this@MainActivity, data.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            onError = { code, msg ->

            }

            onException = { throwable ->

            }

        })
    }
}