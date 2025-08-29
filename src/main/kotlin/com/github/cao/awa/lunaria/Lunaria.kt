package com.github.cao.awa.lunaria

import com.github.cao.awa.lunaria.state.LunariaState
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

class Lunaria<R> {
    companion object {
        fun <R> of(action: Lunaria<R>.() -> R?) = Lunaria(action)
    }
    private val runnable: (Lunaria<R>) -> R?
    private var thread: Thread
    private var result: R? = null
    var isDone: Boolean = false
    var state: LunariaState = LunariaState.RUNNING
    var exception: Throwable? = null
    var exceptionHandler: ((Throwable) -> Unit) = { }

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

    inline fun <reified E: Throwable> withExceptionHandler(crossinline handler: (E) -> Unit): Lunaria<R> {
        this.exceptionHandler = {
            if (it is E) {
                handler(it)
            }
        }
        return this
    }

    fun get(): R? {
        while (true) {
            if (this.isDone) {
                break
            }
            runCatching {
                ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.MILLISECONDS)
            }
        }
        return this.result
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