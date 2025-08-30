package com.github.cao.awa.lunaria.supplier

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.pool.LunariaPool
import com.github.cao.awa.lunaria.state.LunariaState
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

/**
 * A supplier implementation for Lunaria framework that provides asynchronous computation capabilities.
 *
 * @param R The type of result this supplier will produce
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
class SupplierLunaria<R>: Lunaria {
    companion object {
        /**
         * Creates a new SupplierLunaria instance with the given action.
         *
         * @param action The action to be executed by this supplier
         *
         * @return A new SupplierLunaria instance
         *
         * @author cao_awa
         *
         * @since 1.0.0
         */
        fun <R> of(action: SupplierLunaria<R>.() -> R?) = SupplierLunaria(action)
    }

    // The action to be executed by this supplier
    private val runnable: (SupplierLunaria<R>) -> R?
    // The result of the computation
    var result: R? = null
    // Handler for processing the result after completion
    var completeHandler: ((R?) -> R?) = { it }

    /**
     * Constructs a new SupplierLunaria with the given action.
     *
     * @param action The action to be executed by this supplier
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    constructor(action: SupplierLunaria<R>.() -> R?) {
        this.runnable = action
    }

    /**
     * Adds exception handling for a specific exception type.
     *
     * @param E The type of exception to handle
     * @param handler The handler function for the exception
     *
     * @return This SupplierLunaria instance for method chaining
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    inline fun <reified E: Throwable> withException(crossinline handler: (E) -> Unit): SupplierLunaria<R> {
        this.exceptionHandler = {
            if (it is E) {
                handler(it)
            }
        }
        return this
    }

    /**
     * Sets a handler to process the result after completion.
     *
     * @param handler The handler function to process the result
     *
     * @return This SupplierLunaria instance for method chaining
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    fun withComplete(handler: (R?) -> R): SupplierLunaria<R> {
        this.completeHandler = handler
        return this
    }

    /**
     * Retrieves the result of the computation.
     * Blocks until the computation is complete or an exception occurs.
     *
     * @return The result of the computation, possibly null
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    fun get(): R? {
        while (true) {
            if (this.isDone) {
                break
            }
            runCatching {
                // Wait for computation to complete
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS)
            }.exceptionOrNull()?.also { ex: Throwable ->
                this.exception = ex
                handleException()
                markDone()
            }
        }
        markDone()
        return this.completeHandler(this.result)
    }

    /**
     * Implements the Lunaria interface by providing the action to be executed.
     *
     * @return A Runnable that executes the supplier's action
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
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