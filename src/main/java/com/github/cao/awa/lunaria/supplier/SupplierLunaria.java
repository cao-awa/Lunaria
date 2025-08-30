package com.github.cao.awa.lunaria.supplier;

import com.github.cao.awa.lunaria.Lunaria;
import com.github.cao.awa.lunaria.pool.LunariaPool;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A supplier implementation for Lunaria framework that provides asynchronous computation capabilities.
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
public class SupplierLunaria<R> extends Lunaria {
    // The action to be executed by this supplier
    private final Supplier<R> supplier;
    // The result of the computation
    private R result = null;
    // Handler for processing the result after completion
    private Function<R, R> completeHandler = r -> r;

    public static <T> SupplierLunaria<T> of(Supplier<T> action) {
        return new SupplierLunaria<>(action);
    }

    /**
     * Constructs a new SupplierLunaria with the given action.
     *
     * @param supplier The action to be executed by this supplier
     *
     * @author cao_awa
     *
     * @since 1.0.0
     */
    public SupplierLunaria(Supplier<R> supplier) {
        this.supplier = supplier;
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
    public SupplierLunaria<R> withException(Consumer<Throwable> handler) {
        setSetExceptionHandler(handler);
        return this;
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
    public SupplierLunaria<R> withComplete(Function<R, R> handler) {
        this.completeHandler = handler;
        return this;
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
    public R get() {
        while (true) {
            if (this.isDone) {
                break;
            }
            try {
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS);
            } catch (Throwable ex) {
                exceptionHappening(ex);
                markDone();
            }
        }
        markDone();
        return this.completeHandler.apply(this.result);
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
    @Override
    public Runnable getAction() {
        return () -> {
            try {
                this.result = this.supplier.get();
            } catch (Throwable ex) {
               exceptionHappening(ex);
            }
            markDone();
        };
    }
}
