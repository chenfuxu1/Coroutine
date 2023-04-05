package com.example.coroutine.fourth

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2023/4/1 9:47
 *
 * 测试返回多个值的各种方法：集合-序列-挂起函数
 * 需求：异步返回多个值
 **/
class CoroutineTest01 {

    private fun simpleList(): List<Int> = listOf<Int>(1, 2, 3)
    private fun simpleSequence(): Sequence<Int> = sequence {
        for (i in 4..6) {
            // 会将值添加进 sequence 序列中
            yield(i)
        }
    }

    private suspend fun simpleList2(): List<Int> {
        delay(1000)
        return listOf<Int>(1, 2, 3)
    }

    private fun simpleFlow() = flow<Int> {
        for (i in 1..3) {
            // 每隔 1s 返回一个元素
            delay(1000)
            emit(i) // 发射，产生一个元素
        }
    }

    private fun simpleFlow2() = flow<Int> {
        println("flow 开始")
        for (i in 1..3) {
            delay(1000)
            emit(i) // 产生元素
        }
    }

    private fun simpleFlow3() = flow<Int>() {
        println("emit flow started ${Thread.currentThread().name}")
        for (i in 1..3) {
            delay(1000)
            emit(i)
        }
    }

    private fun simpleFlow4() = flow<Int>() {
        withContext(Dispatchers.IO) {
            println("emit flow started ${Thread.currentThread().name}")
            for (i in 1..3) {
                delay(1000)
                emit(i)
            }
        }
    }

    private fun simpleFlow5() = flow<Int>() {
        println("emit flow started ${Thread.currentThread().name}")
        for (i in 1..3) {
            delay(1000)
            emit(i)
        }
    }.flowOn(Dispatchers.Default)

    private fun simpleFlow6() = flow<Int>() {
        for (i in 1..5) {
            delay(1000)
            emit(i)
            println("emitting $i")
        }
    }

    private fun simpleFlow7() = flow<Int>() {
        for (i in 1..3) {
            // 发送元素需要 1s
            delay(1000)
            emit(i)
            println("emitting $i ${Thread.currentThread().name}")
        }
    }

    // 事件源，每隔 1s 产生一个
    private fun events() = (1..3).asFlow().onEach {
        delay(1000)
    }.flowOn(Dispatchers.Default)

    /**
     * 测试使用集合返回多个数
     * 可以返回多个数，但是在统一线程中，不在异步线程
     */
    @Test
    fun `test multiple values01`() {
        simpleList().forEach {
            println(it)
        }
    }

    /**
     * 使用 sequence，可以返回多个值，但还是在统一线程中
     * 相比于 list 的优点是 sequence 是可变的， list 的长度是固定的
     */
    @Test
    fun `test multiple values02`() {
        simpleSequence().forEach {
            println(it)
        }
    }

    /**
     * 返回了多个值，且是异步线程的，但是是一次性返回了
     * 而不是每隔一秒返回一个值
     */
    @Test
    fun `test multiple values03`() = runBlocking {
        simpleList2().forEach {
            println(it)
        }
    }

    /**
     * 通过 flow，每隔 1s 返回一个元素，且是在异步线程
     */
    @Test
    fun `test multiple values04`() = runBlocking {
        launch {
            for (k in 1..3) {
                println("证明主线程没有被阻塞")
                delay(1500)
            }
        }
        // 通过 collect 方法取到每个元素
        simpleFlow().collect {
            println(it)
        }
    }

    /**
     * Flow 是一种类似于序列的冷流，flow 构建器中的代码直到流被收集的
     * 时候才会运行
     */
    @Test
    fun `test flow is cold`() = runBlocking {
        // 创建 flow 时，不会执行 flow 构建器中的代码
        val flow = simpleFlow2()
        println("开始 collect 元素")
        flow.collect {
            println(it)
        }
        println("再次开始 collect 元素")
        flow.collect {
            println(it)
        }
    }

    /**
     * 流的连续性
     * 流的每次单独收集都是按照顺序执行的，除非使用特殊操作符
     * 从上游到下游每个过渡操作符都会处理每个发射出的值，然后再交给
     * 末端操作符
     */
    @Test
    fun `test flow continuation`() = runBlocking<Unit> {
        // 过滤偶数并将其映射到字符串
        (1..5).asFlow().filter {
            println("filter $it")
            it % 2 == 0
        }.map {
            println("map $it")
            "string $it"
        }.collect {
            println("collect $it")
        }
    }

    /**
     * 流构建器
     * 1.flowOf 构建器定义了一个发射固定值的流
     * 2.使用 .asFlow() 扩展函数，可以将各种集合与序列转换为流
     */
    @Test
    fun `test flow builder`() = runBlocking {
        // 方式 1
        flowOf("one", "two", "three")
            .onEach {
                delay(1000)
            }
            .collect {
                println(it)
            }

        // 方式 2
        (1..3).asFlow()
            .collect {
                println(it)
            }
    }

    /**
     * 正常情况下，发射的位置和收集的位置应该是异步线程
     * 例如下载数据，然后更新 UI
     */
    @Test
    fun `test flow context`() = runBlocking {
        simpleFlow3().collect {
            println("Collected $it ${Thread.currentThread().name}")
        }
    }

    /**
     * 使用 withContext 直接改变会报错
     */
    @Test
    fun `test flow context2`() = runBlocking {
        simpleFlow4().collect {
            println("Collected $it ${Thread.currentThread().name}")
        }
    }

    /**
     * flowOn 操作符，该函数用于更改流发射的上下文
     */
    @Test
    fun `test flow context3`() = runBlocking {
        simpleFlow5().collect {
            println("Collected $it ${Thread.currentThread().name}")
        }
    }

    // 指定在 Dispatchers.IO 协程中执行，launchIn 方法，返回的是 job 对象
    @Test
    fun `test flow launch`() = runBlocking {
        events().onEach {
            println("Event $it ${Thread.currentThread().name}")
        }.launchIn(CoroutineScope(Dispatchers.IO)).join()
    }

    // 流的取消，直接取消协程即可
    @Test
    fun `test cancel flow`() = runBlocking {
        // 超过 2500ms，取消该协程
        withTimeoutOrNull(2500) {
            simpleFlow6().collect {
                println(it)
            }
        }
        println("Done")
    }

    /**
     * 流的取消检测
     */
    @Test
    fun `test cancel flow check`() = runBlocking {
        simpleFlow6().collect {
            println(it)
            if (it == 3) cancel()
        }
    }

    /**
     * 流的取消检测
     * 在协程处于繁忙循环的情况下，必须明确检测是否取消
     */
    @Test
    fun `test cancel flow check2`() = runBlocking {
        /**
         * 这种情况 cpu 密集，不会被取消成功
         * 要想取消，需要明确，每次进行检查，但会损耗性能
         */
        // (1..5).asFlow().collect {
        //     println(it)
        //     if (it == 3) cancel()
        // }

        // 加上 cancellable 进行检查
        (1..5).asFlow().cancellable().collect {
            println(it)
            if (it == 3) cancel()
        }
    }

    /**
     * 测试背压
     * 生产效率大于消费的效率，产生背压
     */
    @Test
    fun `test flow press pressure`() = runBlocking {
        // var totalTime = measureTimeMillis {
        //     simpleFlow7().collect {
        //         // 处理元素需要消耗 3s，最终每三秒打印一次
        //         delay(3000)
        //         println("collected $it ${Thread.currentThread().name}")
        //     }
        // }
        // 生产 1s。消费 3s，共计 4 * 3 = 12s
        // println("总共消耗时间：$totalTime ms")

        // 1.优化，使用缓存
        // var totalTime = measureTimeMillis {
        //     simpleFlow7()
        //         .buffer(10)
        //         .collect {
        //         // 处理元素需要消耗 3s，最终每三秒打印一次
        //         delay(3000)
        //         println("collected $it ${Thread.currentThread().name}")
        //     }
        // }
        // 使用缓存优化了 2s 的生产时间，会一次性先生产出来，buffer 是异步的
        // println("总共消耗时间：$totalTime ms")

        // 2.优化，使用异步线程
        var totalTime = measureTimeMillis {
            simpleFlow7()
                .flowOn(Dispatchers.Default)
                .collect {
                    // 处理元素需要消耗 3s，最终每三秒打印一次
                    delay(3000)
                    println("collected $it ${Thread.currentThread().name}")
                }
        }
        // 使用异步线程去请求，更改了协程的上下文，优化了 2s
        println("总共消耗时间：$totalTime ms")
    }

    /**
     * 测试背压
     * conflate：合并发射项，不是对每个值都进行处理
     * collectLatest: 取消并重新发射最后一个值
     */
    @Test
    fun `test flow press pressure2`() = runBlocking {
        // 1.conflate
        // var totalTime = measureTimeMillis {
        //     simpleFlow7()
        //         .conflate()
        //         .collect {
        //             // 处理元素需要消耗 3s，最终每三秒打印一次
        //             delay(3000)
        //             println("collected $it ${Thread.currentThread().name}")
        //         }
        // }
        // println("总共消耗时间：$totalTime ms")

        // 2.collectLatest
        var totalTime = measureTimeMillis {
            simpleFlow7()
                .collectLatest {
                    // 处理元素需要消耗 3s，最终每三秒打印一次
                    delay(3000)
                    println("collected $it ${Thread.currentThread().name}")
                }
        }
        println("总共消耗时间：$totalTime ms")
    }

}