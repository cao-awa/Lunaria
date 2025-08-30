package com.github.cao.awa.lunaria.pool

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

object LunariaPool {
    private val pool: ForkJoinPool = ForkJoinPool()

    fun awaitQuiescence(timeout: Long, unit: TimeUnit) {
        this.pool.awaitQuiescence(timeout, unit)
    }

    fun awaitTermination(timeout: Long, unit: TimeUnit) {
        this.pool.awaitTermination(timeout, unit)
    }
}