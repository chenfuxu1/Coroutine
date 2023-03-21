package com.example.coroutine.first

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coroutine.R
import kotlinx.coroutines.*

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/20 20:30
 *
 * 协程作用域 MainScope 的使用
 **/
class Activity5 : AppCompatActivity() {
    companion object {
        private const val TAG = "Activity5"
    }

    private lateinit var mNameTv: TextView
    private lateinit var mSubmitBtn: Button

    // 协程作用域 mainScope
    private val mMainScope = MainScope() // 方式1

    // class Activity5: AppCompatActivity(), CoroutineScope by MainScope() { // 方式2
    /**
     * 实现CoroutineScope接口的：public val coroutineContext: CoroutineContext
     * 由MainScope中：public fun MainScope(): CoroutineScope = ContextScope(SupervisorJob() + Dispatchers.Main) 返回实现了
     * 相当于Activity5继承了MainScope() 返回的对象
     * 但对象之间不能继承，需要通过接口，因此这是一个委托对象
     * 这样被委托的对象的属性都可以使用了
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        initListener()
    }

    private fun initListener() {
        mSubmitBtn.also {
            /**
             * 方法1
             */
            mMainScope.launch {
                try {// 执行网络任务10s
                    delay(10000)
                } catch (e: Exception) {
                    // 协程取消会抛出异常
                    Log.d(TAG, "cfx exception: $e")
                }
            }

            // 方法2
            // launch {
            //     try {// 执行网络任务10s
            //         delay(10000)
            //     } catch (e: Exception) {
            //         Log.d(TAG, "cfx exception: $e")
            //     }
            // }
        }
    }

    private fun initView() {
        mNameTv = findViewById(R.id.name_tv)
        mNameTv.text = "陈蓉"
        mSubmitBtn = findViewById(R.id.submit_btn)
    }

    override fun onDestroy() {
        super.onDestroy()
        /**
         * 方法1：取消协程
         * 协程作用域被取消，里面的子协程都会被取消
         * 协程取消会抛出异常
         */
        mMainScope.cancel()

        // 方法二取消
        // cancel()
    }
}