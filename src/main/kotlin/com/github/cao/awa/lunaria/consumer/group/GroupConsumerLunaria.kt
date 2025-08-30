package com.github.cao.awa.lunaria.consumer.group

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.pool.LunariaPool
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 * A group consumer implementation that processes collections of inputs in parallel groups.
 *
 * This class divides a collection of inputs into smaller groups and processes them
 * using multiple ConsumerLunaria instances in parallel.
 *
 * @param I The type of elements to be processed
 * @author cao_awa
 * @since 1.0.0
 */
class GroupConsumerLunaria<I: Any>(
    /**
     * The collection of inputs to be processed
     */
    private val inputs: Collection<I>,
    /**
     * The size of each group (number of elements per group)
     */
    private val split: Int,
    /**
     * The action to be performed on each input element
     */
    private val action: (I) -> Unit
): Lunaria() {
    // Split inputs into groups based on the specified split size
    private var groups: List<List<I>> = this.inputs.chunked(this.split)
    // List of executors, one for each group
    private val executors: MutableList<ConsumerLunaria<List<I>>>? = CopyOnWriteArrayList<ConsumerLunaria<List<I>>>()

    init {
        // Initialize executors for each group
        this.groups.forEach { group ->
            this.executors!!.add(ConsumerLunaria(group) { groupedInputs ->
                runCatching {
                    // Process each input in the group
                    for (input: I in groupedInputs) {
                        this.action(input)
                    }
                }.exceptionOrNull()?.also { ex: Throwable ->
                    // Handle any exceptions that occur during processing
                    this.exception = ex
                    handleException()
                    // Cancel all executors if an exception occurs
                    for (executor in this.executors) {
                        runCatching {
                            executor.cancel()
                        }
                    }
                    this.isDone = true
                }
            })
        }
    }

    /**
     * Waits for all executors to complete their processing.
     *
     * This method blocks until all groups have been processed or an error occurs.
     * If an exception occurs during processing, it will be handled and the operation
     * will be marked as done.
     */
    fun await() {
        // Wait for executors to initialize
        while (this.executors == null) {
            runCatching {
                LunariaPool.awaitQuiescence(1, TimeUnit.MILLISECONDS)
            }.exceptionOrNull()?.also { ex: Throwable ->
                this.exception = ex
                handleException()
                this.isDone = true
            }
            markDone()
            return
        }
        // Wait for all executors to complete
        for (executor: ConsumerLunaria<List<I>> in this.executors) {
            executor.await()
        }
        markDone()
    }

    /**
     * Gets the action that will be executed by this consumer.
     *
     * @return A Runnable that executes the await operation and marks the task as done
     */
    override fun getAction(): Runnable {
        return Runnable {
            await()
            markDone()
        }
    }
}