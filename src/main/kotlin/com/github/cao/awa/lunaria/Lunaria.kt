package com.github.cao.awa.lunaria

class Lunaria<R> {
    companion object {
        fun <R> of(action: Lunaria<R>.() -> R?) = Lunaria(action)
        fun <R> ofNoException(action: Lunaria<R>.() -> R?) = Lunaria(action)
    }
    private val runnable: (Lunaria<R>) -> R?
    private var thread: Thread? = null
    private var result: R? = null
    var isDone: Boolean = false

    constructor(action: Lunaria<R>.() -> R?) {
        this.runnable = action
        doTask(action)
    }

    private fun doTask(action: Lunaria<R>.() -> R?) {
        this.thread = Thread.ofVirtual().start {
            this.result = action()
            this.isDone = true
        }
    }

    fun get(): R? {
        while (!this.isDone) {
            Thread.sleep(0, 0)
        }
        return this.result
    }

    fun cancel() {
        runCatching {
            this.thread?.interrupt()
        }
        this.isDone = true
        this.result = null
    }
}