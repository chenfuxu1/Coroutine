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
import kotlin.coroutines.*

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/20 9:44
 *
 * 协程的基础设施层和业务框架层
 *
 **/
class Activity4: AppCompatActivity() {
    companion object {
        private const val TAG = "Activity4"
    }

    private lateinit var mNameTv: TextView
    private lateinit var mSubmitBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initListenerByCoroutine()
    }


    private fun initListenerByCoroutine() {
        /**
         * 携程体
         * 协程的挂起点就是由携程体的上下文记录下来的
         */
        val continuation = suspend {
            // 执行协程的结果，会返回到 resumeWith 中的 result 中
            5
        }.createCoroutine(object: Continuation<Int>{
            // 协程上下文
            override val context: CoroutineContext = EmptyCoroutineContext

            // 相当于回调
            override fun resumeWith(result: Result<Int>) {
                Log.d(TAG, "cfx Coroutine End：$result")
            }

        })
        // 启动协程
        continuation.resume(Unit)

    }

    private fun initView() {
        mNameTv = findViewById(R.id.name_tv)
        mNameTv.text = "陈蓉"
        mSubmitBtn = findViewById(R.id.submit_btn)
    }

}