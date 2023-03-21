package com.example.coroutine.first.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/17 23:24
 *
 **/
object RetrofitManager {
    private val mRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.sunofbeaches.com/shop/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getApi(): UserServiceApi {
        return mRetrofit.create(UserServiceApi::class.java)
    }
}