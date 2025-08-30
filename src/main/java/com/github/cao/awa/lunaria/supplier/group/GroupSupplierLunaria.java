package com.github.cao.awa.lunaria.supplier.group;

import com.github.cao.awa.lunaria.Lunaria;
import com.github.cao.awa.lunaria.pool.LunariaPool;
import com.github.cao.awa.lunaria.supplier.SupplierLunaria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A supplier that groups multiple suppliers together and executes them in parallel.
 * This class splits the work into multiple groups and manages their execution.
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
public class GroupSupplierLunaria<R> extends Lunaria {
    // The total number of suppliers to create
    private final int size;
    // The number of suppliers in each group
    private final int split;
    // The action to be executed by each supplier
    private final Supplier<R> action;
    // List of supplier groups that will execute the actions.
    private final List<List<SupplierLunaria<R>>> executors = new CopyOnWriteArrayList<>();

    public GroupSupplierLunaria(int size, int split, Supplier<R> action) {
        this.size = size;
        this.split = split;
        this.action = action;

        List<SupplierLunaria<R>> grouped = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            grouped.add(SupplierLunaria.of(action));
            if (grouped.size() == split) {
                this.executors.add(grouped);
                grouped = new ArrayList<>();
            }
        }
    }
    /**
     * Retrieves results from all suppliers in all groups.
     * Waits for executors to be initialized if they're not ready.
     *
     * @return List of results from all suppliers, may contain null values
    */
    public List<R> get() {
        // Wait for executors to be initialized
        while (this.executors == null) {
            try {
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS);
            } catch (Throwable ex) {
                exceptionHappening(ex);
                markDone();
            }
            markDone();
            return Collections.emptyList();
        }

        // Collect results from all suppliers in all groups
        final List<R> result = new ArrayList<>();
        for (List<SupplierLunaria<R>> suppliers : this.executors) {
            for (SupplierLunaria<R> supplier : suppliers) {
                result.add(supplier.get());
            }
        }
        markDone();
        return result;
    }

    /**
     * Gets the action to be executed by this supplier group.
     * Currently, returns an empty runnable.
     *
     * @return An empty Runnable instance
     */
    @Override
    public Runnable getAction() {
        return () -> { };
    }
}
