package com.example.coroutine.second

import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/21 10:22
 *
 * 测试协程的启动和取消
 *
 **/
class CoroutineTest01 {

    /**
     * 测试协程的创建
     * 该Test函数是在主线程执行的
     * 下面把主线程包装成一个协程
     * runBlocking会把主线程变成主协程
     *
     * launch: 返回一个job并且不附带任何结果值
     * async：返回一个Deferred，Deferred也是一个job
     * 可以使用await()在一个延期的值上得到它的最终结果
     *
     * runBlocking会等待两个子协程执行完毕
     * 程序才会退出
     */
    @Test
    fun `test coroutine builder`() = runBlocking {
        // launch: 返回一个job并且不附带任何结果值
        val jobLaunch = launch {
            delay(200)
            println("job launch finished")
        }

        // async：返回一个Deferred，Deferred也是一个job
        val jobAsync = async {
            delay(200)
            println("job async finished")
            "job async result"
        }
        // 可以使用await()在一个延期的值上得到它的最终结果
        println(jobAsync.await())
    }

    /**
     * 测试协程等待
     * 等待jobLaunch1执行完了，再执行其他的协程
     */
    @Test
    fun `test coroutine join`() = runBlocking {
        // launch: 返回一个job并且不附带任何结果值
        val jobLaunch1 = launch {
            delay(2000)
            println("job launch1 finished")
        }
        jobLaunch1.join()
        val jobLaunch2 = launch {
            delay(200)
            println("job launch2 finished")
        }
        val jobLaunch3 = launch {
            delay(200)
            println("job launch3 finished")
        }

    }

    /**
     * 测试协程等待
     *
     * join和await都是挂起函数，不会阻塞主线程
     */
    @Test
    fun `test coroutine await`() = runBlocking {
        val jobAsync1 = async {
            delay(2000)
            println("job async1 finished")
        }
        jobAsync1.await()
        val jobAsync2 = async {
            delay(200)
            println("job async2 finished")
        }
        val jobAsync3 = async {
            delay(200)
            println("job async3 finished")
        }
    }

    /**
     * 测试两个函数执行的时间
     */
    @Test
    fun `test async`() = runBlocking {
        // 返回两个协程函数一共执行的时间
        val time = measureTimeMillis {
            val one = doOne()
            val two = doTwo()
            println("The result is ${one + two}") // The result is 33
        }
        /**
         * 先执行one，再执行two，共花费了3013 ms
         */
        println("共花费了：$time ms") // 共花费了：3013 ms
    }

    /**
     * 测试两个函数结构化并发执行的时间
     */
    @Test
    fun `test combine async`() = runBlocking {
        /**
         * 返回两个协程函数一共执行的时间
         * 类似开了两个子线程执行任务
         * 实际都是在主线程中执行
         * 但不会阻塞主线程
         */
        val time = measureTimeMillis {
            val one = async {
                doOne()
            }
            val two = async {
                doTwo()
            }
            println("The result is ${one.await() + two.await()}") // The result is 33

            /**
             * 注意，不要写成下面的写法
             * 这种写法，不会同时执行任务
             * 会先等待one执行完了，再去执行two
             */
            // val one = async {
            //     doOne()
            // }.await()
            // val two = async {
            //     doTwo()
            // }.await()
        }
        /**
         * one 和 two会同时执行，但在一个measureTimeMillis携程中
         * 所以one.await()或者two.await()会等待两个子协程都执行完了，才统计时间
         */
        println("共花费了：$time ms") // 共花费了：2025 ms
    }

    private suspend fun doOne(): Int {
        delay(1000)
        return 11
    }

    private suspend fun doTwo(): Int {
        delay(2000)
        return 22
    }

}