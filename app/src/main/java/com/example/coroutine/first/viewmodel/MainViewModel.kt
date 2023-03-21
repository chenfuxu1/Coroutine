package com.example.coroutine.first.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutine.first.bean.CategoryBean
import com.example.coroutine.first.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/20 22:53
 *
 **/
class MainViewModel : ViewModel() {
    val userLiveData = MutableLiveData<List<CategoryBean.DataDTO?>?>()

    // 持有repository
    private val mUserRepository by lazy {
        UserRepository()
    }

    // 获取到user
    fun getUser() {
        viewModelScope.launch {
            // retrofit 如果是耗时操作，会自动启动一个 IO 的协程
            userLiveData.value = mUserRepository.getUser()
        }
    }
}