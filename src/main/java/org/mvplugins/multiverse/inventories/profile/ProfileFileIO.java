package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

final class ProfileFileIO {

    private final ExecutorService fileIOExecutorService;

    ProfileFileIO() {
        fileIOExecutorService = Executors.newSingleThreadExecutor();
    }

    FileConfiguration waitForConfigHandle(File file) {
        Future<FileConfiguration> future = fileIOExecutorService.submit(new ConfigLoader(file));
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    FileConfiguration getConfigHandleNow(File file) throws IOException, InvalidConfigurationException {
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.options().continueOnSerializationError(true);
        jsonConfiguration.load(file);
        return jsonConfiguration;
    }

    private class ConfigLoader implements Callable<FileConfiguration> {
        private final File file;

        private ConfigLoader(File file) {
            this.file = file;
        }

        @Override
        public FileConfiguration call() throws Exception {
            return getConfigHandleNow(file);
        }
    }

    void queueAction(Runnable action) {
        fileIOExecutorService.submit(action);
    }

    <T> Future<T> queueSupplier(Supplier<T> supplier) {
        return fileIOExecutorService.submit(supplier::get);
    }

    <T> T waitForData(Supplier<T> supplier) {
        try {
            return queueSupplier(supplier).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
