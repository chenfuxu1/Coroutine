package com.example.coroutine.fourth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.lang.ArithmeticException

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2023/4/5 20:46
 *
 * 流的异常处理
 *
 **/
class CoroutineTest03 {
    private fun simpleFlow() = flow<Int> {
        for (i in 1..3) {
            println("emitting $i")
            emit(i)
        }
    }

    /**
     * 下游异常
     * 收集阶段捕获异常
     */
    @Test
    fun `test flow exception`() = runBlocking {
        try {
            simpleFlow().collect {
                println(it)
                check(it <= 1) {
                    "Collected $it"
                }
            }
        } catch (e: Throwable) {
            println("捕获异常：$e")
        }
    }

    /**
     * 上游异常
     * 发射阶段发生异常进行捕获
     * catch 捕获整个函数
     */
    @Test
    fun `test flow exception2`() = runBlocking {
        flow {
            emit(1)
            throw ArithmeticException("Div 0")
        }.catch { e: Throwable ->
            println("caught $e")
            // 发生异常了，重新发送一个
            emit(100)
        }.flowOn(Dispatchers.IO)
            .collect {
                println(it)
            }
    }

    /**
     * 流的完成
     */
    private fun simpleFlow2() = (1..3).asFlow()

    private fun simpleFlow3() = flow<Int> {
        emit(1)
        throw RuntimeException()
    }

    // 命令式
    @Test
    fun `test flow complete in finally`() = runBlocking {
        try {
            simpleFlow2().collect {
                println(it)
            }
        } finally {
            println("流完成了---命令式")
        }
    }

    /**
     * 声明式
     * 优点：
     * 1、可以拿到异常信息，但不会捕获
     * 2、既可以获取上游异常，也能获取下游异常
     */
    @Test
    fun `test flow complete in onCompletion`() = runBlocking {
        simpleFlow2().onCompletion {
            println("流完成了---声明式")
        }.collect {
            println(it)
        }
    }

    @Test
    fun `test flow complete in onCompletion2`() = runBlocking {
        /**
         * 1.捕获 & 获取上游异常
         */
        // simpleFlow3().onCompletion { exception ->
        //     // 只能拿到异常信息，不能捕获，捕获需要 catch 函数
        //     if (exception != null) {
        //         println("发生异常：$exception")
        //     }
        //     // 使用 catch 捕获上游异常
        // }.catch { exception: Throwable ->
        //     println("捕获异常：$exception")
        // }.collect {
        //     println(it)
        // }

        /**
         * 2.下游发生了异常，onCompletion 也能获取到
         * 但要想捕获就要 try/catch 了
         */
        simpleFlow2().onCompletion { exception ->
            // 只能拿到异常信息，不能捕获，捕获需要 catch 函数
            if (exception != null) {
                println("发生异常：$exception")
            }
        }.collect {
            println(it)
            // 下游发生了异常
            check(it <= 1) {
                "Collected $it"
            }
        }
    }
}