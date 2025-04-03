package org.mvplugins.multiverse.inventories.util;

import com.dumptruckman.minecraft.util.Logging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FutureNow {

    private static final long TIMEOUT = TimeUnit.SECONDS.toNanos(10);

    public static <T> T get(CompletableFuture<T> future) {
        try {
            return future.get(TIMEOUT, TimeUnit.NANOSECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Logging.severe("Could not get future as it timed out: " + future);
            throw new RuntimeException(e);
        }
    }
}
