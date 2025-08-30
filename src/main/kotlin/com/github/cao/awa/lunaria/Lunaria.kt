package com.github.cao.awa.lunaria

import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.state.LunariaState
import com.github.cao.awa.lunaria.supplier.SupplierLunaria

abstract class Lunaria {
    private var thread: Thread
    var isDone: Boolean = false
    var state: LunariaState = LunariaState.RUNNING
    var exception: Throwable? = null
    var exceptionHandler: ((Throwable) -> Unit) = { }

    init {
        this.thread = Thread.ofVirtual().start(getAction())
    }

    abstract fun getAction(): Runnable

    fun handleException() {
        if (this.exception != null) {
            this.exceptionHandler(this.exception!!)
        }
        this.state = LunariaState.FAILED
    }

    fun cancel() {
        this.exception = runCatching {
            this.thread.interrupt()
        }.exceptionOrNull()
        this.isDone = true
        this.state = LunariaState.CANCELLED
    }

    fun markDone() {
        this.isDone = true
        this.state = LunariaState.DONE
    }
}