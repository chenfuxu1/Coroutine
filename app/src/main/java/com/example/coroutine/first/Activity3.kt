package com.example.coroutine.first

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutine.R
import com.example.coroutine.first.api.RetrofitManager
import com.example.coroutine.first.bean.CategoryBean
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/20 9:44
 *
 * 挂起和阻塞的区别
 *
 **/
class Activity3: AppCompatActivity() {
    companion object {
        private const val TAG = "Activity3"
    }

    private lateinit var mNameTv: TextView
    private lateinit var mSubmitBtn: Button

    var mData: List<CategoryBean.DataDTO?>? = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initListenerByCoroutine()
    }

    /**
     * 挂起和阻塞的区别
     * 阻塞: 啥也不干，一直等待
     * 挂起：相当于中断，执行完了会通知，从断点继续执行，而主线程不会阻塞，会在这个期间继续执行
     */
    private fun initListenerByCoroutine() {
        mSubmitBtn.also {
            it.setOnClickListener {
                // GlobalScope.launch(Dispatchers.Main) {
                //     // 挂起
                //     delay(12000)
                //     Log.d(TAG, "cfx 这是执行挂起的12s后 ${Thread.currentThread().name}")
                // }

                // 阻塞
                Thread.sleep(12000)
                Log.d(TAG, "cfx 这是执行阻塞的12s后 ${Thread.currentThread().name}")
            }
        }
    }

    private fun initView() {
        mNameTv = findViewById(R.id.name_tv)
        mNameTv.text = "陈蓉"
        mSubmitBtn = findViewById(R.id.submit_btn)
    }


    private suspend fun getUser() {
        val user = get()
        show(user)
    }

    /**
     * 如果要在一个函数里面使用挂起函数，那么外部的函数也要声明为挂起函数suspend
     */
    private suspend fun get() = withContext(Dispatchers.IO) {
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