package com.example.coroutine.second

import kotlinx.coroutines.*
import org.junit.Test
import java.io.BufferedReader
import java.io.FileReader

/**
 * Project: coroutine
 * Create By: ChenFuXu
 * DateTime: 2022/8/21 20:29
 *
 * 协程的取消
 * 1、取消作用域会取消它的子协程
 * 2、被取消的子协程并不会影响其余兄弟协程
 * 3、协程通过掏出一个特殊的异常CancellationException来处理取消操作
 * 4、所有kotlinx.coroutines中的挂起函数(withContext,delay等)都是可取消的
 *
 **/
class CoroutineTest04 {
    /**
     * 1、取消作用域会取消它的子协程
     */
    @Test
    fun `test scope cancel`() = runBlocking<Unit> {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            delay(1000)
            println("job 1 ")
        }
        scope.launch {
            delay(1000)
            println("job 2 ")
        }
        delay(100)
        /**
         * 取消作用域会取消他的所有子协程
         * 所以，都不会打印
         */
        scope.cancel()
        println("主线程结束")
    }

    /**
     * 2、被取消的子协程并不会影响其余兄弟协程
     */
    @Test
    fun `test scope brother cancel`() = runBlocking<Unit> {
        val scope = CoroutineScope(Dispatchers.Default)
        val job1 = scope.launch {
            delay(1000)
            println("job 1 ")
        }
        val job2 = scope.launch {
            delay(1000)
            println("job 2 ")
        }
        delay(100)
        /**
         * job1取消，不会影响job2
         */
        job1.cancel()
        delay(2000)
        println("主线程结束")
    }

    /**
     * 3、协程通过抛出一个特殊的异常CancellationException来处理取消操作
     */
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `test cancellation exception`() = runBlocking {
        /**
         * GlobalScope.launch有自己的上下文，作用域
         * 所以runBlocking不会等待，所以这里不会执行job1
         * 如果想要打出job 1 可以在runBlocking中job1.join()
         */
        val job1 = GlobalScope.launch {
            try {
                delay(1000)
                println("job 1")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        delay(100)
        job1.cancel(CancellationException("取消"))
        // 这样可以打印出job 1
        job1.join()
        // 等价于
        // job1.cancelAndJoin()
    }

    /**
     * CPU密集型任务取消
     */
    @Test
    fun `test cancel cpu task by isActive`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            // while(i < 5 ) {
            while(i < 5 && isActive) {
                // cpu密集型计算任务
                // 一直判断条件是否满足，这时没办法取消任务
                // 但是只要调用job.cancelAndJoin()，isActive就为false
                // 所以while中可以增加条件isActive
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500
                }
            }
        }

        delay(1300)
        println("main: I'm tried of waiting")
        job.cancelAndJoin()
        println("main: now i can quit")
    }

    /**
     * CPU密集型任务取消
     */
    @Test
    fun `test cancel cpu task by ensureActive`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while(i < 5 ) {
                // cpu密集型计算任务
                // 一直判断条件是否满足，这时没办法取消任务
                ensureActive() // 可以取消任务，和isActive本质一样，会抛出异常(异常被静默处理了)
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500
                }
            }
        }

        delay(1300)
        println("main: I'm tried of waiting")
        job.cancelAndJoin()
        println("main: now i can quit")
    }

    /**
     * CPU密集型任务取消
     * yield函数会检查所在协程的状态，如果已经取消，则抛出CancellationException予以响应
     * 此外，它还会尝试让出线程的执行权，给其他协程提供执行机会
     */
    @Test
    fun `test cancel cpu task by yield`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while(i < 5 ) {
                // cpu密集型计算任务
                // 一直判断条件是否满足，这时没办法取消任务
                yield() // 会抛出异常(异常静默处理了)
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500
                }
            }
        }

        delay(1300)
        println("main: I'm tried of waiting")
        job.cancelAndJoin()
        println("main: now i can quit")
    }

    /**
     * 取消协程副作用
     * 需要增加 try/catch 在 finally 中释放资源
     */
    @Test
    fun `test release resources`() = runBlocking {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job: $i")
                    delay(500)
                }
            } finally {
                println("开始释放资源")
            }
        }
        delay(1300)
        println("main: I'm tried of waiting")
        job.cancelAndJoin()
        println("main: now i can quit")
    }

    /**
     * use函数：该函数只能被实现了Closeable的对象使用，程序结束的时候会自动调用
     * close方法，适合文件对象
     * 测试Use函数
     */
    @Test
    fun `test use function`() = runBlocking {
        val br = BufferedReader(FileReader("D:\\cfx.txt"))
        with(br) {
            var line: String?
            // 使用try/finally释放
            try {
                while(true) {
                    line = readLine() ?: break
                    println(line)
                }
            } finally {
                close()
            }
        }

        println("======================")
        // 使用use函数释放资源, use函数会释放资源
        BufferedReader(FileReader("D:\\cfx.txt")).use {
            var line : String?
            while(true) {
                line = it.readLine() ?: break
                println(line)
            }
        }
    }

    /**
     * 不能取消的任务
     * 处于取消中状态的协程不能够挂起(运行不能取消的代码)，当协程被取消后需要调用挂起函数
     * 我们需要将清理任务的代码放置于NonCancellable CoroutineContext中
     * 这样会挂起运行中的代码，并保持协程否取消中状态直到任务处理完成
     */
    @Test
    fun `test can't cancel task`() = runBlocking {
        // val job = launch {
        //     try {
        //         repeat(1000) { i ->
        //             println("job: $i")
        //             delay(500)
        //         }
        //     } finally {
        //         println("开始释放资源")
        //         delay(100) // 2.由于finally中还有挂起函数，所以取消任务失败
        //         println("释放资源成功")
        //     }
        // }
        // delay(1300)
        // println("main: I'm tried of waiting")
        // job.cancelAndJoin() // 1.执行取消任务时，会走到finally中
        // println("main: now i can quit")

        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job: $i")
                    delay(500)
                }
            } finally {
                withContext(NonCancellable) {
                    println("开始释放资源")
                    delay(100) // 2.释放成功
                    println("释放资源成功")
                }
            }
        }
        delay(1300)
        println("main: I'm tried of waiting")
        job.cancelAndJoin() // 1.执行取消任务时，会走到finally中
        println("main: now i can quit")
    }

    /**
     * 超时任务
     * withTimeout：超时抛出异常
     * withTimeoutOrNull：通过返回null来进行超时操作，从而替代抛出异常
     */
    @Test
    fun `deal with time out task`() = runBlocking {
        // 这个任务需要1300ms执行完成
        val result = withTimeoutOrNull(1300) {
            repeat(1000) { i ->
                println("当前的 i：$i")
                delay(500)
            }
            "DONE" // 正常执行完返回
        } ?: "Default" // 如果超时为空，返回默认值
        println(result)
    }
}