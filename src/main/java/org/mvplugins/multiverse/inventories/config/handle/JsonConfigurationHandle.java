package org.mvplugins.multiverse.inventories.config.handle;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.mvplugins.multiverse.core.config.handle.FileConfigurationHandle;
import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Try;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class JsonConfigurationHandle extends FileConfigurationHandle<JsonConfiguration> {

    /**
     * Creates a new builder for {@link JsonConfigurationHandle}.
     *
     * @param configPath    The path to the config file.
     * @param nodes         The nodes.
     * @return The builder.
     */
    public static @NotNull Builder<? extends Builder> builder(@NotNull Path configPath, @NotNull NodeGroup nodes) {
        return new Builder<>(configPath, nodes);
    }

    protected JsonConfigurationHandle(
            @NotNull Path configPath,
            @Nullable Logger logger,
            @NotNull NodeGroup nodes,
            @Nullable ConfigMigrator migrator
    ) {
        super(configPath, logger, nodes, migrator);
    }

    @Override
    protected void loadConfigObject() throws IOException, InvalidConfigurationException {
        config = new JsonConfiguration();
        config.load(configFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Try<Void> save() {
        return Try.run(() -> config = new JsonConfiguration())
                .flatMap(ignore -> super.save())
                .andThenTry(ignore -> config.save(configFile));
    }

    /**
     * Builder for {@link JsonConfigurationHandle}.
     *
     * @param <B>   The type of the builder.
     */
    public static class Builder<B extends Builder<B>> extends FileConfigurationHandle.Builder<JsonConfiguration, B> {

        protected Builder(@NotNull Path configPath, @NotNull NodeGroup nodes) {
            super(configPath, nodes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull JsonConfigurationHandle build() {
            return new JsonConfigurationHandle(configPath, logger, nodes, migrator);
        }
    }
}
