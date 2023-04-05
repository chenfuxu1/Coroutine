package com.example.coroutine.fourth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2023/4/5 11:17
 *
 * 操作符
 **/
class CoroutineTest02 {
    // 转换操作符
    suspend fun performRequest(request: Int): String {
        delay(1000)
        return "response $request"
    }

    @Test
    fun `test transform flow operator`() = runBlocking {
        // (1..3).asFlow()
        //     .map {
        //         // int 类型转换为 string
        //         performRequest(it)
        //     }
        //     .collect {
        //         println(it)
        //     }

        /**
         * 使用 transform, 可以发射多个值
         * 对多个值进行变化处理
         */
        (1..3).asFlow()
            .transform { request ->
                emit("Making request $request")
                emit(performRequest(request))
            }
            .collect {
                println(it)
            }
    }

    // 限长操作符
    fun numbers() = flow<Int> {
        try {
            emit(1)
            emit(2)
            println("This line will not execute")
            emit(3)
        } finally {
            println("Finally in numbers")
        }
    }

    @Test
    fun `test limit length operator`() = runBlocking {
        // 使用 take 函数，只取前面两个元素
        numbers().take(2)
            .collect {
                println(it)
            }
    }

    /**
     * 末端操作符 reduce
     */
    @Test
    fun `test terminal operator`() = runBlocking {
        val sum = (1..5).asFlow()
            // 对流中元素平方操作
            .map { it * it }
            // acc 是累加值，value 是当前值
            .reduce { acc, value ->
                acc + value
            }
        println(sum)
    }

    /**
     * 组合操作符 zip
     */
    @Test
    fun `test zip operator`() = runBlocking {
        val numbers = (1..3).asFlow()
        val strs = flowOf("one", "two", "three")
        numbers.zip(strs) { number, str ->
            "$number -> $str"
        }.collect {
            println(it)
        }
    }

    /**
     * 组合操作符 zip
     */
    @Test
    fun `test zip operator2`() = runBlocking {
        val numbers = (1..3).asFlow().onEach {
            delay(300)
        }
        val strs = flowOf("one", "two", "three").onEach {
            delay(400)
        }
        val startTime = System.currentTimeMillis()
        numbers.zip(strs) { number, str ->
            "$number -> $str"
        }.collect {
            // 因为是异步获取的，所以每个元素获取的最大时间是 400ms
            println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        }
    }

    /**
     * 展平操作符，将多维的 flow 展平
     * flatMapConcat 连接模式
     * flatMapMerge  合并模式
     * flatMapLatest 最新元素展平模式
     */
    private fun requestFlow(i: Int) = flow<String> {
        emit("$i: First")
        delay(500)
        emit("$i: Second")
    }

    /**
     * flatMapConcat 连接模式
     */
    @Test
    fun `test flatMapConcat`() = runBlocking {
        val startTime = System.currentTimeMillis()
        // (1..3).asFlow()
        //     .onEach {
        //         delay(100)
        //     }
        //     /**
        //      * 因为 requestFlow(it) 返回的是 flow，所以这里是
        //      * Flow<Flow<Int>> 相当于二维流
        //      */
        //     .map {
        //         requestFlow(it)
        //     }
        //     .collect {
        //         println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        //     }

        (1..3).asFlow()
            .onEach {
                delay(100)
            }
            .flatMapConcat {
                requestFlow(it)
            }
            .collect {
                println("$it at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }

    /**
     * FlatMapMerge 合并模式
     * 会一维一维元素进行返回，而不是直接返回所有
     */
    @Test
    fun `test flatMapMerge`()  = runBlocking {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach {
                delay(100)
            }
            .flatMapMerge {
                requestFlow(it)
            }
            .collect {
                println("$it at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }

    /**
     * FlatMapLatest 最新展平模式
     * 多维的只返回最新的元素
     */
    @Test
    fun `test flatMapLatest`() = runBlocking {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach {
                delay(100)
            }
            .flatMapLatest {
                requestFlow(it)
            }
            .collect {
                println("$it at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }
}