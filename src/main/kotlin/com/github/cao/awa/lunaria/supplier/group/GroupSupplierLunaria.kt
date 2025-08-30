package com.github.cao.awa.lunaria.supplier.group

import com.github.cao.awa.lunaria.Lunaria
import com.github.cao.awa.lunaria.consumer.ConsumerLunaria
import com.github.cao.awa.lunaria.pool.LunariaPool
import com.github.cao.awa.lunaria.supplier.SupplierLunaria
import java.util.Collections
import java.util.concurrent.TimeUnit

class GroupSupplierLunaria<R: Any>(
    private val split: Int,
    private val action: () -> R?
): Lunaria() {
    private var executors: MutableList<List<SupplierLunaria<R?>>>? = ArrayList()

    init {
        var groups: MutableList<List<SupplierLunaria<R?>>> = ArrayList()
        var group: MutableList<SupplierLunaria<R?>> = ArrayList()
        for (index: Int in 0..this.split) {
            if (index % this.split == 0) {
                groups.add(group)
                group = ArrayList()
            }
            group.add(SupplierLunaria {
                action()
            })
        }
        this.executors = groups
    }

    fun get(): List<R?> {
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
        val result: MutableList<R?> = ArrayList()
        for (suppliers: List<SupplierLunaria<R?>> in this.executors) {
            for (supplier in suppliers) {
                result.add(supplier.get())
            }
        }
        markDone()
        return result
    }

    override fun getAction(): Runnable {
        return Runnable {

        }
    }
}