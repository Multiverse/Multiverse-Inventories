package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.minecraft.util.Logging;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

final class ProfileFileIO {

    private final ExecutorService fileIOExecutorService = Executors.newWorkStealingPool();
    private final Map<File, CountDownLatch> fileLocks = new ConcurrentHashMap<>();

    ProfileFileIO() {
    }

    CompletableFuture<Void> queueAction(File file, Runnable action) {
        CountDownLatch thisLatch = new CountDownLatch(1);
        CountDownLatch toWaitLatch = fileLocks.put(file, thisLatch);
        CompletableFuture<Void> future = new CompletableFuture<>();
        fileIOExecutorService.submit(() -> {
            if (toWaitLatch != null && toWaitLatch.getCount() > 0) {
                try {
                    Logging.finest("Waiting for lock on " + file);
                    toWaitLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            action.run();
            thisLatch.countDown();
            fileLocks.remove(file);
            future.complete(null);
        });
        return future;
    }

    <T> CompletableFuture<T> queueCallable(File file, Supplier<T> callable) {
        CountDownLatch thisLatch = new CountDownLatch(1);
        CountDownLatch toWaitLatch = fileLocks.put(file, thisLatch);
        CompletableFuture<T> future = new CompletableFuture<>();
        fileIOExecutorService.submit(() -> {
            if (toWaitLatch != null && toWaitLatch.getCount() > 0) {
                try {
                    Logging.finest("Waiting for lock on " + file);
                    toWaitLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T result = callable.get();
            thisLatch.countDown();
            fileLocks.remove(file);
            future.complete(result);
        });
        return future;
    }

    <T> T waitForData(File file, Supplier<T> callable) {
        try {
            return queueCallable(file, callable).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    ExecutorService getExecutor() {
        return fileIOExecutorService;
    }
}
