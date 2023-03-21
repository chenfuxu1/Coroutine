package com.example.coroutine.third

import junit.framework.AssertionFailedError
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test
import java.lang.ArithmeticException
import java.util.concurrent.atomic.AtomicReference

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/24 20:03
 *
 * 协程上下文: CoroutineContext是一组用于定义协程行为的元素，它有下面几部分组成
 * Jon：控制协程的生命周期
 * CoroutineDispatcher：向合适的线程分发任务
 * CoroutineName: 协程的名称，调试的时候很有用
 * CoroutineExceptionHandler：处理未被捕捉的异常
 *
 **/
class CoroutineTest01 {
    /**
     * 测试组合协程上下文元素
     * 有时需要在协程上下文中定义多个元素，我们可以使用 + 操作符来实现
     * 比如：我们可以显式指定一个调度器来启动协程并且同时显式指定一个命名
     */
    @Test
    fun `test coroutineContext`() = runBlocking<Unit> {
        launch(Dispatchers.Default + CoroutineName("组合协程上下文") ) {
            println("这是组合协程上下文 ${Thread.currentThread().name}")
        }
    }

    /**
     * 协程上下文的继承
     * 对于新创新的协程，它的CoroutineContext会包含一个全新的Job实例，他会帮助我们控制协程的生命周期
     * 而剩下的元素会从CoroutineContext的父类继承
     * 该父类可能是另外一个协程或者创建该协程的CoroutineScope
     */
    @Test
    fun `test coroutineContext extend`() = runBlocking<Unit> {
        val scope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("协程上下文继承"))
        // launch 是 scope 作用域下面的协程，除了 Job 其余的都来自 scope
        val job = scope.launch {
            // 新的协程会将 CoroutineScope 作为父类
            println("${coroutineContext[Job]} == ${Thread.currentThread().name}")
            // async 是 launch 的之协程，除了 Job 其余的都来自 launch
            val result = async {
                // 通过 async 创建的新协程会将当前协程作为父级
                println("${coroutineContext[Job]} == ${Thread.currentThread().name}")
            }.await()
        }
        job.join()
    }

    /**
     * 协程上下文的继承
     * 协程的上下文 = 默认值 + 继承的 CoroutineContext + 参数
     * 一些元素包含默认值：Dispatchers.Default 是默认的 CoroutineDispatchers，以及
     * coroutine 是默认的 CoroutineName
     *
     * 继承的 CoroutineContext 是 CoroutineScope 或者其父协程的 CoroutineContext
     *
     * 传入协程构建器的参数的优先级高于继承的上下文参数，因此会覆盖对应的参数值
     */
    @Test
    fun `test coroutineContext extend two`() = runBlocking<Unit> {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("异常：$exception")
        }
        val scope = CoroutineScope (Job() + Dispatchers.Main + coroutineExceptionHandler)
        // 新的 CoroutineContext = 父级 CoroutineContext + Job()
        val job = scope.launch(Dispatchers.IO) {
            // 新协程
            println("${coroutineContext[Job]} == ${Thread.currentThread().name}")

        }.join()
    }

    /**
     * 异常的传播
     * 协程构建器有两种形式：自动传播异常(launch与actor)，向用户暴露异常(async与produce)
     * 当这些构建器用于创建一个根协程时(该协程不是另一个协程的子协程)，前者这类构建器，异常会在它
     * 发生的第一时间抛出，而后者依赖用户最终消费异常，例如通过await或receive
     */
    @Test
    fun `test exception propagation`() = runBlocking {
        val job = GlobalScope.launch {
            try {
                // 通过launch的根协程，异常在发生的第一时间抛出
                throw IndexOutOfBoundsException()
            } catch (e: Exception) {
                println("捕获异常：IndexOutOfBoundsException")
            }
        }
        job.join()

        val deferred = GlobalScope.async {
            throw ArithmeticException()
        }
        try {
            // 通过async构建的根协程，依赖用户消费，在调用await时抛出
            deferred.await()
        } catch (e: Exception) {
            println("捕获异常：ArithmeticException")
        }
    }

    /**
     * 非根协程的异常
     * 其他协程所创建的协程中，产生的异常总是会被传播
     */
    @Test
    fun `test exception propagation2`() = runBlocking {
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            async {
                try {
                    /**
                     * 非根协程的异常，会立即传播
                     * 如果 async 抛出异常，launch 就会立即抛出异常，而不会调用 .await()
                     */
                    throw IllegalArgumentException()
                } catch (e: Exception) {
                    println("捕获异常：$e")
                }
                "OK"
            }
        }
        job.join()
    }

    /**
     * 测试 supervisorJob
     * 一个子协程的运行失败不会影响到其他子协程
     * supervisor不会传播异常给它的父级
     * 它会让子协程自己处理异常
     */
    @Test
    fun `test supervisorJob`() = runBlocking {
        // 2、如果这里的 SupervisorJob() 换成 Job(), 那么都会退出
        val supervisor = CoroutineScope(SupervisorJob())
        val job1 = supervisor.launch {
            delay(100)
            println("子协程job1")
            // 1、这里 job1 抛出异常了，不会影响到 job2，job2 仍在运行
            throw IllegalArgumentException()
        }
        val job2 = supervisor.launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("子协程2退出")
            }
        }

        delay(200)
        // supervisor.cancel() 也会使所有子协程都退出
        joinAll(job1, job2)
    }

    /**
     * supervisorScope
     * 当作业自身执行失败的时候，所有的子作业将会被全部取消
     */
    @Test
    fun `test supervisorScope`() = runBlocking {
        supervisorScope {
            launch {
                delay(100)
                println("子任务1")
                // 此处抛出的异常，不会影响到子任务2
                throw IllegalArgumentException()
            }

            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("子任务2执行完了")
            }
        }
    }

    /**
     * supervisorScope
     * 当作业自身执行失败的时候，所有的子作业将会被全部取消
     */
    @Test
    fun `test supervisorScope2`() = runBlocking<Unit> {
        supervisorScope {
            val job1 = launch {
                try {
                    println("这是子任务1")
                    delay(Long.MAX_VALUE)
                } finally {
                    println("子任务1被取消")
                }
            }
            // 让一下执行权，不然会一直执行子任务1
            yield()
            println("作用域中开始抛出异常")
            // 由于这里执行失败，子作业任务1也会失败
            throw AssertionError()

        }
    }

    /**
     * 捕获异常的
     */
    @Test
    fun `test coroutine exception handler`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("捕获到了异常：$exception")
        }

        val job = GlobalScope.launch(handler) {
            println("这是job")
            throw AssertionError()
        }

        val deferred = GlobalScope.async(handler) {
            throw ArithmeticException()
        }

        job.join()
        deferred.await()
    }
}