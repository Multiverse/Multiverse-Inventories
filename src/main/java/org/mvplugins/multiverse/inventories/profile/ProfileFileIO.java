package org.mvplugins.multiverse.inventories.profile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

final class ProfileFileIO {

    private final ExecutorService fileIOExecutorService;
    private final Map<File, CountDownLatch> fileLocks = new ConcurrentHashMap<>();

    ProfileFileIO() {
        fileIOExecutorService = Executors.newWorkStealingPool();
    }

    @SuppressWarnings("unchecked")
    Future<Void> queueAction(File file, Runnable action) {
        CountDownLatch thisLatch = new CountDownLatch(1);
        CountDownLatch toWaitLatch = fileLocks.put(file, thisLatch);
        return (Future<Void>) fileIOExecutorService.submit(() -> {
            if (toWaitLatch != null) {
                try {
                    toWaitLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            action.run();
            thisLatch.countDown();
            fileLocks.remove(file);
        });
    }

    <T> Future<T> queueCallable(File file, Supplier<T> callable) {
        CountDownLatch thisLatch = new CountDownLatch(1);
        CountDownLatch toWaitLatch = fileLocks.put(file, thisLatch);
        return fileIOExecutorService.submit(() -> {
            if (toWaitLatch != null) {
                try {
                    toWaitLatch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            T result = callable.get();
            thisLatch.countDown();
            fileLocks.remove(file);
            return result;
        });
    }

    <T> T waitForData(File file, Supplier<T> callable) {
        try {
            return queueCallable(file, callable).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
