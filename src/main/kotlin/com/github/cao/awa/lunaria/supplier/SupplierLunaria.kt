package com.github.cao.awa.lunaria.supplier

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.pool.LunariaPool
import com.github.cao.awa.lunaria.state.LunariaState
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

class SupplierLunaria<R>: Lunaria {
    companion object {
        fun <R> of(action: SupplierLunaria<R>.() -> R?) = SupplierLunaria(action)
    }
    private val runnable: (SupplierLunaria<R>) -> R?
    var result: R? = null
    var completeHandler: ((R?) -> R?) = { it }

    constructor(action: SupplierLunaria<R>.() -> R?) {
        this.runnable = action
    }

    inline fun <reified E: Throwable> withException(crossinline handler: (E) -> Unit): SupplierLunaria<R> {
        this.exceptionHandler = {
            if (it is E) {
                handler(it)
            }
        }
        return this
    }

    fun withComplete(handler: (R?) -> R): SupplierLunaria<R> {
        this.completeHandler = handler
        return this
    }

    fun get(): R? {
        while (true) {
            if (this.isDone) {
                break
            }
            runCatching {
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS)
            }.exceptionOrNull()?.also { ex: Throwable ->
                this.exception = ex
                handleException()
                this.isDone = true
            }

        }
        markDone()
        return this.completeHandler(this.result)
    }

    override fun getAction(): Runnable {
       return Runnable {
           runCatching {
               this.result = this.runnable(this)
           }.exceptionOrNull().also { exception: Throwable? ->
               this.exception = exception
               handleException()
           }
           markDone()
        }
    }
}