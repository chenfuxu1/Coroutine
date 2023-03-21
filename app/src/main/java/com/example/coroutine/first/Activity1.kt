package com.example.coroutine.first

import android.annotation.SuppressLint
import android.os.AsyncTask
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


class Activity1 : AppCompatActivity() {
    companion object {
        private const val TAG = "Activity1"
    }

    private lateinit var mNameTv: TextView
    private lateinit var mSubmitBtn: Button

    var mData: List<CategoryBean.DataDTO?>? = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        executeTask()
        initView()
        // initListener()
        initListenerByCorountine()
    }


    // 1、使用AsyncTask完成异步任务
    @SuppressLint("StaticFieldLeak")
    private fun initListener() {
        mSubmitBtn.also {
            it.setOnClickListener {
                object: AsyncTask<Void, Void, List<CategoryBean.DataDTO?>?>() {
                    // 执行异步任务,子线程
                    @SuppressLint("StaticFieldLeak")
                    override fun doInBackground(vararg params: Void?): List<CategoryBean.DataDTO?>? {
                        return mData
                    }

                    // 返回的结果回调，主线程，更新UI
                    override fun onPostExecute(result: List<CategoryBean.DataDTO?>?) {
                        val dataDTO = result?.get(0)
                        mNameTv.text = dataDTO?.title
                    }
                }.execute()
            }
        }
    }

    /**
     * 使用携程完成异步任务
     */
    private fun initListenerByCorountine() {
        mSubmitBtn.also {
            it.setOnClickListener {
                /**
                 * GlobalScope: 顶级携程
                 * launch：携程构建器
                 * Dispatchers.Main：表示当前的作用域在主线程
                 */
                GlobalScope.launch(Dispatchers.Main) {
                    /**
                     * 携程的任务调度器，表示在IO线程，因为是获取网络数据
                     * 协程的方便之处在于不用使用回调，在获取完数据后，返回的 data 自动的切回到当前的外部线程
                     * 即主线程，所以此处直接更新数据是没有问题的
                     */
                    val data = withContext(Dispatchers.IO) {
                        mData
                    }
                    mNameTv.text = data?.get(0)?.title
                }
            }
        }
    }

    private fun initView() {
        mNameTv = findViewById(R.id.name_tv)
        mNameTv.text = "陈蓉"
        mSubmitBtn = findViewById(R.id.submit_btn)
    }

    // 执行网络任务
    private fun executeTask() {
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

    // 执行网络任务(携程挂起函数suspend)
    // 协程让异步逻辑同步化，杜绝回调地狱
    // 协程最核心的点就是，函数或者一段程序能够被挂起，稍后再在挂起的位置恢复
    suspend fun executeTask2() {
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