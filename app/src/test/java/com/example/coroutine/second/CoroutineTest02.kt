package com.example.coroutine.second

import kotlinx.coroutines.*
import org.junit.Test

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/21 16:04
 *
 * 测试协程的启动模式
 * DEFAULT：协程创建后，立即开始调度，在调度前如果协程被取消，其将直接进入取消向应的状态
 * ATOMIC: 协程创建后，立即开始调度，协程执行到第一个挂起点之前不响应取消
 * LAZY: 只有协程被需要时，包括主动调用协程的start，join或者await等函数时才会开始调度
 * 如果调度前就被取消，那么协程将直接进入异常结束状态
 * UNDISPATCHED: 协程创建后立即在当前函数调用栈中执行，直到遇到第一个真正挂起的点
 **/
class CoroutineTest02 {
    /**
     * 测试协程的启动模式
     */
    @Test
    fun `test coroutine start mode`() = runBlocking {
        val jobDefault = launch(start = CoroutineStart.DEFAULT) {
            delay(10000)
            println("jobDefault 执行结束")
        }
        delay(1000)
        jobDefault.cancel() // 不会等待子协程，直接进入协程取消状态，然后会被取消
        println("取消协程")
    }

    /**
     * ATOMIC: 协程创建后，立即开始调度，协程执行到第一个挂起点之前不响应取消
     * 使用对象：
     * 例如在delay(1000)之前有必须要完成的操作，使用这种模式
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test coroutine start mode atomic`() = runBlocking {
        val jobAtomic = launch(start = CoroutineStart.ATOMIC) {
            // 假设还有其他代码执行
            // ...
            // ...

            delay(10000) // 这是挂起函数，挂起点
            println("jobAtomic 执行结束")
        }
        delay(1000)
        jobAtomic.cancel() // 在没有执行到第一个挂起函数，挂起点（delay(10000)）的位置，取消是不会响应的
        println("取消协程")
    }

    /**
     * LAZY: 只有协程被需要时，包括主动调用协程的start，join或者await等函数时才会开始调度
     * 如果调度前就被取消，那么协程将直接进入异常结束状态
     */
    @Test
    fun `test coroutine start mode lazy`(): Unit = runBlocking {
        // 1、定义协程
        val jobLazy = async(start = CoroutineStart.LAZY) {
            300
        }
        // 2、执行其他任务
        // ...

        // 3、开始启动, 任何时候调用取消，都会被取消
        jobLazy.await()
    }

    /**
     * UNDISPATCHED: 协程创建后立即在当前函数调用栈中执行，直到遇到第一个真正挂起的点
     * UNDISPATCHED表示不转发
     *
     * 如何在通过Dispatchers.IO让使用的协程仍然在主线程中？
     * 可以使用UNDISPATCHED启动模式，因为协程创建后立即在当前函数调用栈中执行
     * 优先级高
     */
    @Test
    fun `test coroutine start mode undispatched`() = runBlocking {
        // 1、定义协程
        val jobUndispatched1 = async(context = Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            println("cfx 当前线程：${Thread.currentThread().name}")
        }

        val jobUndispatched2 = async(context = Dispatchers.IO, start = CoroutineStart.DEFAULT) {
            println("cfx 当前线程：${Thread.currentThread().name}")
        }

    }
}