package com.example.coroutine.first

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutine.R
import com.example.coroutine.first.api.RetrofitManager
import com.example.coroutine.first.bean.CategoryBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/20 9:44
 *
 **/
class Activity2: AppCompatActivity() {
    companion object {
        private const val TAG = "Activity2"
    }

    private lateinit var mNameTv: TextView
    private lateinit var mSubmitBtn: Button

    var mData: List<CategoryBean.DataDTO?>? = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        executeTask()
        initView()
        initListenerByCoroutine()
    }

    /**
     * 使用携程完成异步任务
     */
    private fun initListenerByCoroutine() {
        mSubmitBtn.also {
            it.setOnClickListener {
                GlobalScope.launch(Dispatchers.Main) {
                    Log.d(TAG, "cfx 11111111")
                    getUser()
                    Log.d(TAG, "cfx 22222222222")
                }
                Log.d(TAG, "cfx 77777777")
            }
        }
    }

    private fun initView() {
        mNameTv = findViewById(R.id.name_tv)
        mNameTv.text = "陈蓉"
        mSubmitBtn = findViewById(R.id.submit_btn)
    }

    // 执行网络任务(携程挂起函数suspend)
    // 协程让异步逻辑同步化，杜绝回调地狱
    // 协程最核心的点就是，函数或者一段程序能够被挂起，稍后再在挂起的位置恢复
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


    private suspend fun getUser() {
        Log.d(TAG, "cfx 333333333333")
        val user = get()
        Log.d(TAG, "cfx 555555555")
        show(user)
        Log.d(TAG, "cfx 666666666")
    }

    /**
     * 如果要在一个函数里面使用挂起函数，那么外部的函数也要声明为挂起函数suspend
     */
    private suspend fun get() = withContext(Dispatchers.IO) {
        Log.d(TAG, "cfx 4444444444")
        mData
    }

    // 主线程更新UI
    private fun show(user: List<CategoryBean.DataDTO?>?) {
        mNameTv.text = user?.get(0)?.title

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}