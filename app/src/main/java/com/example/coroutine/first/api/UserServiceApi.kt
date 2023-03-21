package com.example.coroutine.first.api

import com.example.coroutine.first.bean.CategoryBean
import retrofit2.Call

import retrofit2.http.GET


/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/17 23:09
 *
 **/
interface UserServiceApi {
    @GET("discovery/categories")
    fun getCategories(): Call<CategoryBean>
}