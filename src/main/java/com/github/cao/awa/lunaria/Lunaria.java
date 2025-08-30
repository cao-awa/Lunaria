package com.github.cao.awa.lunaria;

import com.github.cao.awa.lunaria.status.LunariaStatus;

import java.util.function.Consumer;

/**
 * The abstract base class for Lunaria framework operations.
 * This class provides core functionality for managing virtual thread-based operations
 * with state tracking and exception handling capabilities.
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
abstract public class Lunaria {
    /** The virtual thread that executes the Lunaria operation */
    private Thread thread;
    /** Flag indicating whether the operation is completed */
    public boolean isDone;
    /** Current status of the Lunaria operation */
    private LunariaStatus status = LunariaStatus.RUNNING;
    private Throwable exception;
    private Consumer<Throwable> exceptionHandler;

    public Lunaria() {
        // Initialize the virtual thread with the action provided by getAction()
        this.thread = Thread.ofVirtual().start(getAction());
    }

    public LunariaStatus getStatus() {
        return this.status;
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
    public abstract Runnable getAction();

    /**
     * Handles any exception that occurred during execution.
     * Invokes the exception handler and updates the operation state to FAILED.
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public void requestHandleException() {
        if (this.exception != null && this.exceptionHandler != null) {
            this.exceptionHandler.accept(this.exception);
        }
        this.status = LunariaStatus.FAILED;
    }

    public void exceptionHappening(Throwable ex) {
        this.exception = ex;
        requestHandleException();
    }

    /**
     * Cancels the current operation.
     * Attempts to interrupt the thread and updates the operation state.
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public void cancel() {
        try {
            this.thread.interrupt();
            markDone();
            this.status = LunariaStatus.CANCELLED;
            return;
        } catch (Throwable ex) {
            this.exception = ex;
        }
        markDone();
        this.status = LunariaStatus.FAILED;
    }

    /**
     * Marks the operation as completed successfully.
     * Updates the operation state to DONE.
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public void markDone()  {
        this.isDone = true;
        this.status = LunariaStatus.DONE;
    }

    public void setSetExceptionHandler(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
    }
}