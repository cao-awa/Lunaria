package com.github.cao.awa.lunaria.consumer

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.state.LunariaState
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

class ConsumerLunaria<I>: Lunaria {
    companion object {
        val pool: ForkJoinPool = ForkJoinPool()

        fun <I> of(input: I, action: (I) -> Unit) = ConsumerLunaria(input, action)
    }
    private val runnable: (I) -> Unit
    private val input: I

    constructor(input: I, action: (I) -> Unit) {
        this.input = input
        this.runnable = action
    }

    inline fun <reified E: Throwable> withException(crossinline handler: (E) -> Unit): Lunaria {
        this.exceptionHandler = {
            if (it is E) {
                handler(it)
            }
        }
        return this
    }

    fun await() {
        while (true) {
            if (this.isDone) {
                break
            }
            runCatching {
                pool.awaitQuiescence(1, TimeUnit.MILLISECONDS)
            }.exceptionOrNull()?.also { ex: Throwable ->
                this.exception = ex
                handleException()
                this.isDone = true
            }
        }
    }

    override fun getAction(): Runnable {
       return Runnable {
            runCatching {
                this.runnable(this.input)
            }.exceptionOrNull().also { exception: Throwable? ->
                this.exception = exception
                handleException()
            }
            this.isDone = true
            this.state = LunariaState.DONE
        }
    }
}