package com.github.cao.awa.lunaria.consumer

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.pool.LunariaPool
import com.github.cao.awa.lunaria.state.LunariaState
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * A consumer-based implementation of the Lunaria interface that handles input processing and exception handling.
 *
 * @param I The type of input that this consumer will process
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
class ConsumerLunaria<I>: Lunaria {
    companion object {
        /**
         * Creates a new ConsumerLunaria instance with the specified input and action.
         *
         * @param I The type of input
         * @param input The input data to be processed
         * @param action The action to be performed on the input
         * @return A new ConsumerLunaria instance
         */
        fun <I> of(input: I, action: Consumer<I>) = ConsumerLunaria(input, action)
    }

    // The action to be executed on the input
    private val runnable: Consumer<I>
    // The input data to be processed
    private val input: I

    /**
     * Constructs a new ConsumerLunaria with the specified input and action.
     *
     * @param input The input data to be processed
     * @param action The action to be performed on the input
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    constructor(input: I, action: Consumer<I>) {
        this.input = input
        this.runnable = action
    }

    /**
     * Adds exception handling for a specific type of exception.
     *
     * @param E The type of exception to handle
     * @param handler The handler function for the exception
     *
     * @return This Lunaria instance for method chaining
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    inline fun <reified E: Throwable> withException(crossinline handler: (E) -> Unit): Lunaria {
        this.exceptionHandler = {
            if (it is E) {
                handler(it)
            }
        }
        return this
    }

    /**
     * Blocks the current thread until the consumer task completes its execution.
     *
     * This method provides a synchronization point in asynchronous operations,
     * ensuring that all task processing is completed before proceeding.
     *
     * Features:
     * - Monitors task completion status
     * - Provides automatic exception propagation
     * - Ensures proper resource cleanup
     *
     * Note: If an exception occurs during task execution, it will be captured
     * and processed through the configured exception handler.
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    fun await() {
        while (true) {
            // Check if the task has already completed
            if (this.isDone) {
                break
            }

            // Try to wait for 1ms using the LunariaPool
            runCatching {
                // Use minimal wait time to maintain responsiveness
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS)
            }.exceptionOrNull()?.also { ex: Throwable ->
                // Store the exception and handle it if one occurs
                this.exception = ex
                handleException()
                // Mark the task as done since we encountered an exception
                markDone()
            }
        }
    }

    /**
     * Gets the runnable action that will be executed by this consumer.
     *
     * @return A Runnable that wraps the consumer's action and exception handling
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    override fun getAction(): Runnable {
       return Runnable {
            runCatching {
                this.runnable.accept(this.input)
            }.exceptionOrNull().also { exception: Throwable? ->
                this.exception = exception
                handleException()
                markDone()
            }
           markDone()
        }
    }
}