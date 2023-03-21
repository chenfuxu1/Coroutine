package com.example.coroutine.first.repository

import android.util.Log
import com.example.coroutine.first.Activity2
import com.example.coroutine.first.api.RetrofitManager
import com.example.coroutine.first.bean.CategoryBean
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/20 23:14
 *
 * 请求网络数据
 **/
class UserRepository {
    private var mData: List<CategoryBean.DataDTO?>? = mutableListOf()

    companion object {
        private const val TAG = "UserRepository"
    }

    init {
        executeTask()
    }

    suspend fun getUser(): List<CategoryBean.DataDTO?>?{
        return mData
    }

    fun executeTask() {
        val api = RetrofitManager.getApi()
        val task = api.getCategories()
        task.enqueue(object : Callback<CategoryBean> {
            override fun onResponse(call: Call<CategoryBean?>?, response: Response<CategoryBean?>) {
                val responseCode: Int = response.code()
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 获取数据成功
                    Log.d(TAG, "cfx getCategories 获取数据成功")
                    response.body()?.getData()?.forEach {
                        Log.d(TAG, "cfx it.title = " + it?.title)
                    }
                    Log.d(TAG, "cfx Thread.currentThread() " + Thread.currentThread())
                    mData = response.body()?.getData()

                } else {
                    // 获取数据失败
                    Log.d(TAG, "cfx getCategories 获取数据失败 " + response.errorBody().toString())

                }
            }

            override fun onFailure(call: Call<CategoryBean?>?, t: Throwable) {
                Log.e(TAG, "cfx getCategories 请求错误 $t")

            }
        })
    }
}