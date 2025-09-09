package com.example.network.api

import com.example.http.http.model.BaseResponse
import com.example.network.bean.HuangliResponse
import com.example.network.bean.NewsResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    /**
     * ef4480ce866214af80bb9e77b78e4a5c
     */
    @GET("toutiao/content")
    fun toutiaoContent(
        @Query("uniquekey") uniquekey: String,
        @Query("key") key: String
    ): Call<BaseResponse<NewsResponse>>

    /**
     * 7bac7d66c2b37f23a087f5b86c3b373f
     */
    @FormUrlEncoded
    @POST("laohuangli/d")
    fun laohuangli(
        @Field("key") key: String,
        @Field("date") date: String
    ): Call<BaseResponse<HuangliResponse>>


}