package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.minecraft.util.Logging;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service
final class AsyncFileIO {

    private final ExecutorService fileIOExecutorService = Executors.newWorkStealingPool();
    private final Map<File, CountDownLatch> fileLocks = new ConcurrentHashMap<>();

    CompletableFuture<Void> queueAction(Runnable action) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        fileIOExecutorService.submit(() -> {
            Try.runRunnable(action)
                    .onFailure(future::completeExceptionally)
                    .onSuccess(ignore -> future.complete(null));
        });
        return future;
    }

    <T> CompletableFuture<T> queueCallable(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        fileIOExecutorService.submit(() -> {
            Try.ofSupplier(supplier)
                    .onFailure(future::completeExceptionally)
                    .onSuccess(future::complete);
        });
        return future;
    }

    CompletableFuture<Void> queueFilesAction(File[] files, Runnable action) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        fileIOExecutorService.submit(() -> {
            for (File file : files) {
                CountDownLatch toWaitLatch = fileLocks.put(file, new CountDownLatch(1));
                waitForLock(file, toWaitLatch);
            }
            Try<Void> tryResult = Try.runRunnable(action);
            for (File file : files) {
                CountDownLatch thisLatch = fileLocks.remove(file);
                if (thisLatch != null) {
                    thisLatch.countDown();
                }
            }
            tryResult.onFailure(future::completeExceptionally).onSuccess(ignore -> future.complete(null));
        });
        return future;
    }

    CompletableFuture<Void> queueFileAction(File file, Runnable action) {
        CountDownLatch thisLatch = new CountDownLatch(1);
        CountDownLatch toWaitLatch = fileLocks.put(file, thisLatch);
        CompletableFuture<Void> future = new CompletableFuture<>();
        fileIOExecutorService.submit(() -> {
            waitForLock(file, toWaitLatch);
            Try<Void> tryResult = Try.runRunnable(action);
            fileLocks.remove(file);
            thisLatch.countDown();
            tryResult.onFailure(future::completeExceptionally).onSuccess(ignore -> future.complete(null));
        });
        return future;
    }

    <T> CompletableFuture<T> queueFileCallable(File file, Supplier<T> supplier) {
        CountDownLatch thisLatch = new CountDownLatch(1);
        CountDownLatch toWaitLatch = fileLocks.put(file, thisLatch);
        CompletableFuture<T> future = new CompletableFuture<>();
        fileIOExecutorService.submit(() -> {
            waitForLock(file, toWaitLatch);
            Try<T> tryResult = Try.ofSupplier(supplier);
            fileLocks.remove(file);
            thisLatch.countDown();
            tryResult.onFailure(future::completeExceptionally).onSuccess(future::complete);
        });
        return future;
    }

    private void waitForLock(File file, CountDownLatch toWaitLatch) {
        if (toWaitLatch != null && toWaitLatch.getCount() > 0) {
            try {
                Logging.finest("Waiting for lock on " + file);
                toWaitLatch.await(10, TimeUnit.SECONDS);
                Logging.finest("Aquired lock on " + file);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    ExecutorService getExecutor() {
        return fileIOExecutorService;
    }
}
