package com.github.cao.awa.lunaria

import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.state.LunariaState
import com.github.cao.awa.lunaria.supplier.SupplierLunaria

/**
 * The abstract base class for Lunaria framework operations.
 * This class provides core functionality for managing virtual thread-based operations
 * with state tracking and exception handling capabilities.
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
abstract class Lunaria {
    /** The virtual thread that executes the Lunaria operation */
    private var thread: Thread
    /** Flag indicating whether the operation is completed */
    var isDone: Boolean = false
    /** Current state of the Lunaria operation */
    var state: LunariaState = LunariaState.RUNNING
    /** Stores any exception that occurred during execution */
    var exception: Throwable? = null
    /** Handler for processing exceptions that occur during execution */
    var exceptionHandler: ((Throwable) -> Unit) = { }

    init {
        // Initialize the virtual thread with the action provided by getAction()
        this.thread = Thread.ofVirtual().start(getAction())
    }

    /**
     * Abstract method that must be implemented to provide the core operation logic.
     *
     * @return A Runnable containing the operation to be executed
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    abstract fun getAction(): Runnable

    /**
     * Handles any exception that occurred during execution.
     * Invokes the exception handler and updates the operation state to FAILED.
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    fun handleException() {
        if (this.exception != null) {
            this.exceptionHandler(this.exception!!)
        }
        this.state = LunariaState.FAILED
    }

    /**
     * Cancels the current operation.
     * Attempts to interrupt the thread and updates the operation state.
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    fun cancel() {
        this.exception = runCatching {
            this.thread.interrupt()
        }.exceptionOrNull()
        this.isDone = true
        this.state = LunariaState.CANCELLED
    }

    /**
     * Marks the operation as completed successfully.
     * Updates the operation state to DONE.
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    fun markDone() {
        this.isDone = true
        this.state = LunariaState.DONE
    }
}