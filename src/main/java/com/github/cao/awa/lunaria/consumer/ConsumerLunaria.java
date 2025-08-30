package com.github.cao.awa.lunaria.consumer;

import com.github.cao.awa.lunaria.Lunaria;
import com.github.cao.awa.lunaria.pool.LunariaPool;
import com.github.cao.awa.lunaria.supplier.SupplierLunaria;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A consumer-based implementation of the Lunaria interface that handles input processing and exception handling.
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
public class ConsumerLunaria<I> extends Lunaria {
    // The action to be executed on the input
    private final Consumer<I> runnable;
    // The input data to be processed
    private final I input;

    public static <T> ConsumerLunaria<T> of(T input, Consumer<T> action) {
        return new ConsumerLunaria<>(input, action);
    }

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
    public ConsumerLunaria(I input, Consumer<I> action) {
        this.input = input;
        this.runnable = action;
    }

    /**
     * Adds exception handling for a specific exception type.
     *
     * @param handler The handler function for the exception
     *
     * @return This SupplierLunaria instance for method chaining
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public ConsumerLunaria<I> withException(Consumer<Throwable> handler) {
        setSetExceptionHandler(handler);
        return this;
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
    public void await() {
        while (!this.isDone) {
            try {
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS);
            } catch (Throwable ex) {
                exceptionHappening(ex);
                markDone();
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
    public Runnable getAction() {
        return () -> {
            try {
                this.runnable.accept(this.input);
            } catch (Throwable ex) {
                exceptionHappening(ex);
                markDone();
            }
            markDone();
        };
    }
}
