package com.example.coroutine.second

import kotlinx.coroutines.*
import org.junit.Test

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/21 17:38
 *
 * coroutineScope: 一个协程失败了，其他所有的兄弟协程也会被取消
 * supervisorScope：一个协程失败了，不会影响到其他兄弟协程
 *
 **/
class CoroutineTest03 {
    /**
     * coroutineScope: 一个协程失败了，其他所有的兄弟协程也会被取消
     */
    @Test
    fun `test coroutine scope`() = runBlocking {
        coroutineScope {
            val job1 = launch {
                delay(400)
                println("job1 finished")
            }
            val job2 = launch {
                delay(200)
                println("job2 finished")
                "job2 result"
                throw IllegalArgumentException()
            }
        }
    }

    /**
     * supervisorScope：一个协程失败了，不会影响到其他兄弟协程
     */
    @Test
    fun `test supervisor scope`() = runBlocking {
        supervisorScope {
            val job1 = launch {
                delay(400)
                println("job1 finished")
            }
            val job2 = launch {
                delay(200)
                println("job2 finished")
                "job2 result"
                throw IllegalArgumentException()
            }
        }
    }

}