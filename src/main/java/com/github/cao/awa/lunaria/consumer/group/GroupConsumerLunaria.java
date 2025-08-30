package com.github.cao.awa.lunaria.consumer.group;

import com.github.cao.awa.lunaria.Lunaria;
import com.github.cao.awa.lunaria.consumer.ConsumerLunaria;
import com.github.cao.awa.lunaria.pool.LunariaPool;
import com.github.cao.awa.lunaria.supplier.group.GroupSupplierLunaria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GroupConsumerLunaria<I> extends Lunaria {
    // The collection of inputs to be processes
    private final Collection<I> inputs;
    // The size of each group (number of elements per group).
    private final int split;
    // The action to be performed on each input element.
    private final Consumer<I> action;
    // Split inputs into groups based on the specified split size
    private final List<List<I>> groups;
    // List of executors, one for each group
    private final List<ConsumerLunaria<List<I>>> executors = new CopyOnWriteArrayList<>();

    public GroupConsumerLunaria(Collection<I> inputs, int split, Consumer<I> action) {
        this.inputs = inputs;
        this.split = split;
        this.action = action;
        this.groups = new ArrayList<>();

        List<I> grouped = new ArrayList<>();
        for (int index = 0; index < inputs.size(); index++) {
            grouped.add(((List<I>)inputs).get(index));
            if (grouped.size() == split) {
                this.groups.add(grouped);
                grouped = new ArrayList<>();
            }
        }

        // Initialize executors for each group
        for (List<I> group : this.groups) {
            this.executors.add(ConsumerLunaria.of(group, inputList -> {
                try {
                    // Process each input in the group
                    for (I input : inputList) {
                        this.action.accept(input);
                    }
                } catch (Throwable ex) {
                    // Handle any exceptions that occur during processing
                    exceptionHappening(ex);
                    for (ConsumerLunaria<List<I>> executor : this.executors) {
                        try {
                            // Cancel all executors if an exception occurs
                            executor.cancel();
                        } catch (Throwable ignored) {

                        }
                    }
                    markDone();
                }
            }));
        }
    }

    public void await() {
        // Wait for executors to initialize
        while (true) {
            if (this.executors != null) {
                break;
            }
            try {
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS);
            } catch (Throwable ex) {
                exceptionHappening(ex);
                markDone();
            }
        }
        // Wait for all executors to complete
        for (ConsumerLunaria<List<I>> executor : this.executors) {
            executor.await();
        }
        markDone();
    }

    /**
     * Gets the action that will be executed by this consumer.
     *
     * @return A Runnable that executes the await operation and marks the task as done
     */
    @Override
    public Runnable getAction() {
        return () -> {
            await();
            markDone();
        };
    }
}
