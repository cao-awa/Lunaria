package com.github.cao.awa.lunaria.consumer.group

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.pool.LunariaPool
import java.util.concurrent.TimeUnit

class GroupConsumerLunaria<I: Any>(
    private val inputs: Collection<I>,
    private val split: Int,
    private val action: (I) -> Unit
): Lunaria() {
    private var groups: List<List<I>> = this.inputs.chunked(this.split)
    private val executors: MutableList<ConsumerLunaria<List<I>>>? = ArrayList<ConsumerLunaria<List<I>>>()

    init {
        this.groups.forEach { group ->
            this.executors!!.add(ConsumerLunaria(group) { groupedInputs ->
                runCatching {
                    for (input: I in groupedInputs) {
                        this.action(input)
                    }
                }.exceptionOrNull()?.also { ex: Throwable ->
                    this.exception = ex
                    handleException()
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

    fun await() {
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
        for (executor: ConsumerLunaria<List<I>> in this.executors) {
            executor.await()
        }
        markDone()
    }

    override fun getAction(): Runnable {
        return Runnable {
            await()
            markDone()
        }
    }
}