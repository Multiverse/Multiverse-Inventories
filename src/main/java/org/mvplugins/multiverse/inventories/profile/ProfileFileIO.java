package org.mvplugins.multiverse.inventories.profile;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class ProfileFileIO {

    private final ExecutorService fileIOExecutorService;

    ProfileFileIO() {
        fileIOExecutorService = Executors.newSingleThreadExecutor();
    }

    Future<Void> queueAction(Runnable action) {
        return (Future<Void>) fileIOExecutorService.submit(action);
    }

    <T> Future<T> queueCallable(Callable<T> callable) {
        return fileIOExecutorService.submit(callable);
    }

    <T> T waitForData(Callable<T> callable) {
        try {
            return queueCallable(callable).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
