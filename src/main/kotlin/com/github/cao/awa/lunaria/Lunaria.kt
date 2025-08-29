package com.github.cao.awa.lunaria

import com.github.cao.awa.lunaria.state.LunariaState
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

class Lunaria<R> {
    companion object {
        val pool: ForkJoinPool = ForkJoinPool()

        fun <R> of(action: Lunaria<R>.() -> R?) = Lunaria(action)
    }
    private val runnable: (Lunaria<R>) -> R?
    private var thread: Thread
    var result: R? = null
    var isDone: Boolean = false
    var state: LunariaState = LunariaState.RUNNING
    var exception: Throwable? = null
    var exceptionHandler: ((Throwable) -> Unit) = { }
    var completeHandler: ((R?) -> R?) = { it }

    constructor(action: Lunaria<R>.() -> R?) {
        this.runnable = action
        this.thread = Thread.ofVirtual().start {
            runCatching {
                this.result = action(this)
            }.exceptionOrNull().also { exception: Throwable? ->
                this.exception = exception
                handleException()
            }
            this.isDone = true
            this.state = LunariaState.DONE
        }
    }

    private fun handleException() {
        if (this.exception != null) {
            this.exceptionHandler(this.exception!!)
        }
        this.state = LunariaState.FAILED
    }

    inline fun <reified E: Throwable> withException(crossinline handler: (E) -> Unit): Lunaria<R> {
        this.exceptionHandler = {
            if (it is E) {
                handler(it)
            }
        }
        return this
    }

    fun withComplete(handler: (R?) -> R): Lunaria<R> {
        this.completeHandler = handler
        return this
    }

    fun get(): R? {
        while (true) {
            if (this.isDone) {
                break
            }
            runCatching {
                this.isDone = pool.awaitQuiescence(1, TimeUnit.MILLISECONDS)
            }.exceptionOrNull()?.also { ex: Throwable ->
                this.exception = ex
                handleException()
                this.isDone = true
            }
        }
        return this.completeHandler(this.result)
    }

    fun cancel() {
        this.exception = runCatching {
            this.thread.interrupt()
        }.exceptionOrNull()
        this.isDone = true
        this.state = LunariaState.CANCELLED
        this.result = null
    }
}