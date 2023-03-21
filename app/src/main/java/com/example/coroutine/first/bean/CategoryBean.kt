package com.example.coroutine.first.bean

import com.google.gson.annotations.SerializedName




/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/17 23:18
 *
 **/
class CategoryBean {
    @SerializedName("success")
    private var success: Boolean? = null

    @SerializedName("code")
    private var code: Int? = null

    @SerializedName("message")
    private var message: String? = null

    @SerializedName("data")
    private var data: List<DataDTO?>? = null

    fun isSuccess(): Boolean? {
        return success
    }

    fun setSuccess(success: Boolean?) {
        this.success = success
    }

    fun getCode(): Int? {
        return code
    }

    fun setCode(code: Int?) {
        this.code = code
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    override fun toString(): String {
        return "CategoryBean{success=$success, code=$code, message='$message', data=$data}"
    }

    fun getData(): List<DataDTO?>? {
        return data
    }

    fun setData(data: List<DataDTO?>?) {
        this.data = data
    }

    class DataDTO  {
        @SerializedName("id")
        var id: Int? = null

        @SerializedName("title")
        var title: String? = null

        override fun toString(): String {
            return "DataDTO{id=$id, title='$title'}"
        }
    }
}