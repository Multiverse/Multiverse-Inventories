package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.file.FileConfiguration;
import org.mvplugins.multiverse.external.vavr.control.Try;

import java.io.File;
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

    FileConfiguration waitForConfigHandle(File file) {
        return waitForData(() -> getConfigHandleNow(file));
    }

    FileConfiguration getConfigHandleNow(File file) {
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.options().continueOnSerializationError(true);
        Try.run(() -> jsonConfiguration.load(file)).getOrElseThrow(e -> {
            Logging.severe("Could not load file: " + file);
            throw new RuntimeException(e);
        });
        return jsonConfiguration;
    }
}
