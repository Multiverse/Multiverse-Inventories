package org.mvplugins.multiverse.inventories.config;

import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.config.handle.CommentedConfigurationHandle;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.migration.action.MoveMigratorAction;
import org.mvplugins.multiverse.core.config.migration.VersionMigrator;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
                .migrator(ConfigMigrator.builder(this.configNodes.version)
                        .addVersionMigrator(VersionMigrator.builder(5.0)
                                .addAction(MoveMigratorAction.of("settings.first_run", "first-run"))
                                .addAction(MoveMigratorAction.of("settings.use_bypass", "share-handling.enable-bypass-permissions"))
                                .addAction(MoveMigratorAction.of("settings.default_ungrouped_worlds", "share-handling.default-ungrouped-worlds"))
                                .addAction(MoveMigratorAction.of("settings.save_load_on_log_in_out", "performance.apply-playerdata-on-join"))
                                .addAction(MoveMigratorAction.of("settings.use_game_mode_profiles", "share-handling.enable-gamemode-share-handling"))
                                .addAction(MoveMigratorAction.of("shares.optionals_for_ungrouped_worlds", "share-handling.use-optionals-for-ungrouped-worlds"))
                                .addAction(MoveMigratorAction.of("shares.use_optionals", "share-handling.active-optional-shares"))
                                .build())
                        .build())
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
     * @return True if we should check for bypass permissions.
     */
    public boolean getEnableBypassPermissions() {
        return this.configHandle.get(configNodes.enableBypassPermissions);
    }

    /**
     * @param useBypass Whether or not to check for bypass permissions.
     */
    public Try<Void> setEnableBypassPermissions(boolean useBypass) {
        return this.configHandle.set(configNodes.enableBypassPermissions, useBypass);
    }

    /**
     * @return True if using separate data for game modes.
     */
    public boolean getEnableGamemodeShareHandling() {
        return this.configHandle.get(configNodes.enableGamemodeShareHandling);
    }

    /**
     * @param useGameModeProfile whether to use separate data for game modes.
     */
    public Try<Void> setEnableGamemodeShareHandling(boolean useGameModeProfile) {
        return this.configHandle.set(configNodes.enableGamemodeShareHandling, useGameModeProfile);
    }

    /**
     * @return true if worlds with no group should be considered part of the default group.
     */
    public boolean getDefaultUngroupedWorlds() {
        return this.configHandle.get(configNodes.defaultUngroupedWorlds);
    }

    /**
     * @param useDefaultGroup Set this to true to use the default group for ungrouped worlds.
     */
    public Try<Void> setDefaultUngroupedWorlds(boolean useDefaultGroup) {
        return this.configHandle.set(configNodes.defaultUngroupedWorlds, useDefaultGroup);
    }

    /**
     * Whether Multiverse-Inventories will utilize optional shares in worlds that are not grouped.
     *
     * @return true if should utilize optional shares in worlds that are not grouped.
     */
    public boolean getUseOptionalsForUngroupedWorlds() {
        return this.configHandle.get(configNodes.useOptionalsForUngroupedWorlds);
    }

    /**
     * Sets whether Multiverse-Inventories will utilize optional shares in worlds that are not grouped.
     *
     * @param usingOptionalsForUngrouped true if should utilize optional shares in worlds that are not grouped.
     */
    public Try<Void> setUseOptionalsForUngroupedWorlds(final boolean usingOptionalsForUngrouped) {
        return this.configHandle.set(configNodes.useOptionalsForUngroupedWorlds, usingOptionalsForUngrouped);
    }

    /**
     * @return A list of optional {@link Sharable}s to be treated as
     *         regular {@link Sharable}s throughout the code.
     *         A {@link Sharable} marked as optional is ignored if it is not
     *         contained in this list.
     */
    public Shares getActiveOptionalShares() {
        return this.configHandle.get(configNodes.activeOptionalShares);
    }

    /**
     * Sets the optional shares to be used.
     *
     * @param shares    The optional shares to be used.
     * @return True if successful.
     */
    public Try<Void> setActiveOptionalShares(Shares shares) {
        return this.configHandle.set(configNodes.activeOptionalShares, shares);
    }

    public boolean getUseImprovedRespawnLocationDetection() {
        return this.configHandle.get(configNodes.useImprovedRespawnLocationDetection);
    }

    public Try<Void> setUseImprovedRespawnLocationDetection(boolean useImprovedRespawnLocationDetection) {
        return this.configHandle.set(configNodes.useImprovedRespawnLocationDetection, useImprovedRespawnLocationDetection);
    }

    public boolean getResetLastLocationOnDeath() {
        return this.configHandle.get(configNodes.resetLastLocationOnDeath);
    }

    public Try<Void> setResetLastLocationOnDeath(boolean resetLastLocationOnDeath) {
        return this.configHandle.set(configNodes.resetLastLocationOnDeath, resetLastLocationOnDeath);
    }

    public boolean getApplyLastLocationForAllTeleports() {
        return this.configHandle.get(configNodes.applyLastLocationForAllTeleports);
    }

    public Try<Void> setApplyLastLocationForAllTeleports(boolean applyLastLocationForAllTeleports) {
        return this.configHandle.set(configNodes.applyLastLocationForAllTeleports, applyLastLocationForAllTeleports);
    }

    public boolean getUseByteSerializationForInventoryData() {
        return this.configHandle.get(configNodes.useByteSerializationForInventoryData);
    }

    public Try<Void> setUseByteSerializationForInventoryData(boolean useByteSerializationForInventoryData) {
        return this.configHandle.set(configNodes.useByteSerializationForInventoryData, useByteSerializationForInventoryData);
    }

    public boolean getApplyPlayerdataOnJoin() {
        return this.configHandle.get(configNodes.applyPlayerdataOnJoin);
    }

    public Try<Void> setApplyPlayerdataOnJoin(boolean applyPlayerdataOnJoin) {
        return this.configHandle.set(configNodes.applyPlayerdataOnJoin, applyPlayerdataOnJoin);
    }

    public boolean getAlwaysWriteWorldProfile() {
        return this.configHandle.get(configNodes.alwaysWriteWorldProfile);
    }

    public Try<Void> setAlwaysWriteWorldProfile(boolean alwaysWriteWorldProfile) {
        return this.configHandle.set(configNodes.alwaysWriteWorldProfile, alwaysWriteWorldProfile);
    }

    public List<String> getPreloadDataOnJoinWorlds() {
        return this.configHandle.get(configNodes.preloadDataOnJoinWorlds);
    }

    public Try<Void> setPreloadDataOnJoinWorlds(List<String> preloadDataOnJoinWorlds) {
        return this.configHandle.set(configNodes.preloadDataOnJoinWorlds, preloadDataOnJoinWorlds);
    }

    public List<String> getPreloadDataOnJoinGroups() {
        return this.configHandle.get(configNodes.preloadDataOnJoinGroups);
    }

    public Try<Void> setPreloadDataOnJoinGroups(List<String> preloadDataOnJoinGroups) {
        return this.configHandle.set(configNodes.preloadDataOnJoinGroups, preloadDataOnJoinGroups);
    }

    public int getPlayerFileCacheSize() {
        return this.configHandle.get(configNodes.playerFileCacheSize);
    }

    public Try<Void> setPlayerFileCacheSize(int playerFileCacheSize) {
        return this.configHandle.set(configNodes.playerFileCacheSize, playerFileCacheSize);
    }

    public int getPlayerFileCacheExpiry() {
        return this.configHandle.get(configNodes.playerFileCacheExpiry);
    }

    public Try<Void> setPlayerFileCacheExpiry(int playerFileCacheExpiry) {
        return this.configHandle.set(configNodes.playerFileCacheExpiry, playerFileCacheExpiry);
    }

    public int getPlayerProfileCacheSize() {
        return this.configHandle.get(configNodes.playerProfileCacheSize);
    }

    public Try<Void> setPlayerProfileCacheSize(int playerProfileCacheSize) {
        return this.configHandle.set(configNodes.playerProfileCacheSize, playerProfileCacheSize);
    }

    public int getPlayerProfileCacheExpiry() {
        return this.configHandle.get(configNodes.playerProfileCacheExpiry);
    }

    public Try<Void> setPlayerProfileCacheExpiry(int playerProfileCacheExpiry) {
        return this.configHandle.set(configNodes.playerProfileCacheExpiry, playerProfileCacheExpiry);
    }

    public int getGlobalProfileCacheSize() {
        return this.configHandle.get(configNodes.globalProfileCacheSize);
    }

    public Try<Void> setGlobalProfileCacheSize(int globalProfileCacheSize) {
        return this.configHandle.set(configNodes.globalProfileCacheSize, globalProfileCacheSize);
    }

    public int getGlobalProfileCacheExpiry() {
        return this.configHandle.get(configNodes.globalProfileCacheExpiry);
    }

    public Try<Void> setGlobalProfileCacheExpiry(int globalProfileCacheExpiry) {
        return this.configHandle.set(configNodes.globalProfileCacheExpiry, globalProfileCacheExpiry);
    }

    /**
     * Get the value of registerPapiHook
     *
     * @return The value of registerPapiHook
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public boolean getRegisterPapiHook() {
        return this.configHandle.get(configNodes.registerPapiHook);
    }

    /**
     * Sets the value of registerPapiHook
     *
     * @param registerPapiHook The value of registerPapiHook
     * @return The result of the operation
     *
     * @since 5.1
     */
    @ApiStatus.AvailableSince("5.1")
    public Try<Void> setRegisterPapiHook(boolean registerPapiHook) {
        return this.configHandle.set(configNodes.registerPapiHook, registerPapiHook);
    }

    @ApiStatus.AvailableSince("5.2")
    public int getMaxInventoryItemsSize() {
        return this.configHandle.get(configNodes.maxInventoryItemsSize);
    }

    @ApiStatus.AvailableSince("5.2")
    public Try<Void> setMaxInventoryItemsSize(int maxInventoryItemsSize) {
        return this.configHandle.set(configNodes.maxInventoryItemsSize, maxInventoryItemsSize);
    }

    @ApiStatus.AvailableSince("5.2")
    public int getMaxEnderChestItemsSize() {
        return this.configHandle.get(configNodes.maxEnderChestItemsSize);
    }

    @ApiStatus.AvailableSince("5.2")
    public Try<Void> setMaxEnderChestItemsSize(int maxEnderChestItemsSize) {
        return this.configHandle.set(configNodes.maxEnderChestItemsSize, maxEnderChestItemsSize);
    }

    @ApiStatus.AvailableSince("5.2")
    public int getMaxArmorItemsSize() {
        return this.configHandle.get(configNodes.maxArmorItemsSize);
    }

    @ApiStatus.AvailableSince("5.2")
    public Try<Void> setMaxArmorItemsSize(int maxArmorItemsSize) {
        return this.configHandle.set(configNodes.maxArmorItemsSize, maxArmorItemsSize);
    }

    /**
     * Tells whether this is the first time the plugin has run as set by a config flag.
     *
     * @return True if first_run is set to true in config.
     */
    public boolean getFirstRun() {
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
     * Saves the configuration file to disk.
     */
    public Try<Void> save() {
        return this.configHandle.save();
    }
}

