package com.lee.rest.beachcongestionpj

import android.provider.ContactsContract.Data
import android.view.View
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitCoroutineService {

    @POST("getBeachCongestionApi.do")
    fun beachCongestion(): Call<ResponseBody>

    companion object {
        var retrofitService: RetrofitCoroutineService? = null
        fun getInstance() : RetrofitCoroutineService {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(TARGET_ADDRESS)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(RetrofitCoroutineService::class.java)
            }
            return retrofitService!!
        }

    }

}