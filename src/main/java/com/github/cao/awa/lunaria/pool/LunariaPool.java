package com.github.cao.awa.lunaria.pool;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class LunariaPool {
    private static final ForkJoinPool POOL = new ForkJoinPool();

    public static boolean awaitQuiescence(long timeout, TimeUnit unit) {
       return POOL.awaitQuiescence(timeout, unit);
    }

    public static boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return POOL.awaitTermination(timeout, unit);
    }
}
