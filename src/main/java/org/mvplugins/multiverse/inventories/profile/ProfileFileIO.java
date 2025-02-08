package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
final class ProfileFileIO {

    private final ExecutorService fileIOExecutorService = Executors.newWorkStealingPool();

    @Inject
    public ProfileFileIO() {
    }

    FileConfiguration waitForConfigHandle(File file) {
        Future<FileConfiguration> future = fileIOExecutorService.submit(new ConfigLoader(file));
        while (true) {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
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

    Future<Void> queueWrite(Runnable action) {
        return fileIOExecutorService.submit(new FileWriter(action));
    }

    private static class FileWriter implements Callable<Void> {
        private final Runnable action;

        private FileWriter(Runnable action) {
            this.action = action;
        }

        @Override
        public Void call() {
            action.run();
            return null;
        }
    }
}
