package com.github.cao.awa.lunaria.supplier.group

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.pool.LunariaPool
import com.github.cao.awa.lunaria.supplier.SupplierLunaria
import java.util.Collections
import java.util.concurrent.TimeUnit

/**
 * A supplier that groups multiple suppliers together and executes them in parallel.
 * This class splits the work into multiple groups and manages their execution.
 *
 * @param R The type of result produced by the suppliers
 * @param size The total number of suppliers to create
 * @param split The number of suppliers in each group
 * @param action The action to be executed by each supplier
 *
 * @author cao_awa
 *
 * @since 1.0.0
 */
class GroupSupplierLunaria<R: Any>(
    private val size: Int,
    private val split: Int,
    private val action: () -> R?
): Lunaria() {
    // List of supplier groups that will execute the actions
    private var executors: MutableList<List<SupplierLunaria<R?>>>? = ArrayList()

    init {
        // Initialize the groups of suppliers
        var groups: MutableList<List<SupplierLunaria<R?>>> = ArrayList()
        var group: MutableList<SupplierLunaria<R?>> = ArrayList()
        // Create suppliers up to the specified total size
        for (index: Int in 0..this.size) {
            // Create a new group when the current group reaches the split size
            if (index % this.split == 0) {
                groups.add(group)
                group = ArrayList()
            }
            // Add a new supplier with the given action to the current group
            group.add(SupplierLunaria {
                action()
            })
        }
        this.executors = groups
    }

    /**
     * Retrieves results from all suppliers in all groups.
     * Waits for executors to be initialized if they're not ready.
     *
     * @return List of results from all suppliers, may contain null values
     */
    fun get(): List<R?> {
        // Wait for executors to be initialized
        while (this.executors == null) {
            runCatching {
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS)
            }.exceptionOrNull()?.also { ex: Throwable ->
                this.exception = ex
                handleException()
                this.isDone = true
            }
            markDone()
            return Collections.emptyList()
        }

        // Collect results from all suppliers in all groups
        val result: MutableList<R?> = ArrayList()
        for (suppliers: List<SupplierLunaria<R?>> in this.executors) {
            for (supplier in suppliers) {
                result.add(supplier.get())
            }
        }
        markDone()
        return result
    }

    /**
     * Gets the action to be executed by this supplier group.
     * Currently, returns an empty runnable.
     *
     * @return An empty Runnable instance
     */
    override fun getAction(): Runnable {
        return Runnable {
            // No operation
        }
    }
}