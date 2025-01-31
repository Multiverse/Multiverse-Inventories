package org.mvplugins.multiverse.inventories.config;

import com.dumptruckman.minecraft.util.Logging;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.configuration.handle.CommentedConfigurationHandle;
import org.mvplugins.multiverse.core.configuration.handle.StringPropertyHandle;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Provides methods for interacting with the configuration of Multiverse-Inventories.
 */
@Service
public final class InventoriesConfig {

    public static final String CONFIG_FILENAME = "config.yml";

    private final InventoriesConfigNodes configNodes;
    private final CommentedConfigurationHandle configHandle;
    private final StringPropertyHandle stringPropertyHandle;

    @Inject
    InventoriesConfig(MultiverseInventories inventories) throws IOException {
        this.configNodes = new InventoriesConfigNodes();
        var configPath = Path.of(inventories.getDataFolder().getPath(), CONFIG_FILENAME);
        this.configHandle = CommentedConfigurationHandle.builder(configPath, this.configNodes.getNodes())
                .logger(Logging.getLogger())
                .build();
        this.stringPropertyHandle = new StringPropertyHandle(this.configHandle);
    }

    public Try<Void> load() {
        return this.configHandle.load();
    }

    public FileConfiguration getConfig() {
        return this.configHandle.getConfig();
    }

    public StringPropertyHandle getStringPropertyHandle() {
        return stringPropertyHandle;
    }

    /**
     * Retrieves the locale string from the config.
     *
     * @return The locale string.
     */
    public String getLocale() {
        return this.configHandle.get(configNodes.locale);
    }

    public Try<Void> setLocale(String locale) {
        return this.configHandle.set(configNodes.locale, locale);
    }

    /**
     * Tells whether this is the first time the plugin has run as set by a config flag.
     *
     * @return True if first_run is set to true in config.
     */
    public boolean isFirstRun() {
        return this.configHandle.get(configNodes.firstRun);
    }

    /**
     * Sets the first_run flag in the config so that the plugin no longer thinks it is the first run.
     *
     * @param firstRun What to set the flag to in the config.
     */
    public Try<Void> setFirstRun(boolean firstRun) {
        return this.configHandle.set(configNodes.firstRun, firstRun);
    }

    /**
     * @return True if we should check for bypass permissions.
     */
    public boolean isUsingBypass() {
        return this.configHandle.get(configNodes.useBypass);
    }

    /**
     * @param useBypass Whether or not to check for bypass permissions.
     */
    public Try<Void> setUsingBypass(boolean useBypass) {
        return this.configHandle.set(configNodes.useBypass, useBypass);
    }

    /**
     * Tells whether Multiverse-Inventories should save on player logout and load on player login.
     *
     * @return True if should save and load on player log out and in.
     */
    public boolean usingLoggingSaveLoad() {
        return this.configHandle.get(configNodes.loggingSaveLoad);
    }

    /**
     * Sets whether Multiverse-Inventories should save on player logout and load on player login.
     *
     * @param useLoggingSaveLoad true if should save and load on player log out and in.
     */
    public Try<Void> setUsingLoggingSaveLoad(boolean useLoggingSaveLoad) {
        return this.configHandle.set(configNodes.loggingSaveLoad, useLoggingSaveLoad);
    }

    /**
     * @return A list of optional {@link Sharable}s to be treated as
     *         regular {@link Sharable}s throughout the code.
     *         A {@link Sharable} marked as optional is ignored if it is not
     *         contained in this list.
     */
    public Shares getOptionalShares() {
        return this.configHandle.get(configNodes.optionalShares);
    }

    /**
     * Sets the optional shares to be used.
     *
     * @param shares    The optional shares to be used.
     * @return True if successful.
     */
    public Try<Void> setOptionalShares(Shares shares) {
        return this.configHandle.set(configNodes.optionalShares, shares);
    }

    /**
     * @return true if worlds with no group should be considered part of the default group.
     */
    public boolean isDefaultingUngroupedWorlds() {
        return this.configHandle.get(configNodes.defaultUngroupedWorlds);
    }

    /**
     * @param useDefaultGroup Set this to true to use the default group for ungrouped worlds.
     */
    public Try<Void> setDefaultingUngroupedWorlds(boolean useDefaultGroup) {
        return this.configHandle.set(configNodes.defaultUngroupedWorlds, useDefaultGroup);
    }

    /**
     * @return True if using separate data for game modes.
     */
    public boolean isUsingGameModeProfiles() {
        return this.configHandle.get(configNodes.useGameModeProfiles);
    }

    /**
     * @param useGameModeProfile whether to use separate data for game modes.
     */
    public Try<Void> setUsingGameModeProfiles(boolean useGameModeProfile) {
        return this.configHandle.set(configNodes.useGameModeProfiles, useGameModeProfile);
    }

    /**
     * Whether Multiverse-Inventories will utilize optional shares in worlds that are not grouped.
     *
     * @return true if should utilize optional shares in worlds that are not grouped.
     */
    public boolean usingOptionalsForUngrouped() {
        return this.configHandle.get(configNodes.useOptionalsForUngrouped);
    }

    /**
     * Sets whether Multiverse-Inventories will utilize optional shares in worlds that are not grouped.
     *
     * @param usingOptionalsForUngrouped true if should utilize optional shares in worlds that are not grouped.
     */
    public Try<Void> setUsingOptionalsForUngrouped(final boolean usingOptionalsForUngrouped) {
        return this.configHandle.set(configNodes.useOptionalsForUngrouped, usingOptionalsForUngrouped);
    }

    /**
     * Saves the configuration file to disk.
     */
    public Try<Void> save() {
        return this.configHandle.save();
    }
}

