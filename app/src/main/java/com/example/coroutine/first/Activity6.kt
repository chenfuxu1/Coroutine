package com.example.coroutine.first

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.coroutine.R
import com.example.coroutine.databinding.ActivityMainDatabindingBinding
import com.example.coroutine.first.viewmodel.MainViewModel
import kotlinx.coroutines.*

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/20 20:30
 *
 **/
class Activity6 : AppCompatActivity(), CoroutineScope by MainScope() {
    companion object {
        private const val TAG = "Activity6"
    }

    private val mViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding =
            DataBindingUtil.setContentView<ActivityMainDatabindingBinding>(this, R.layout.activity_main_databinding)
        binding.viewModel = mViewModel
        binding.lifecycleOwner = this
        binding.submitBtn.setOnClickListener {
            // 请求网络数据,通过viewModel
            mViewModel.getUser()
        }
    }


}