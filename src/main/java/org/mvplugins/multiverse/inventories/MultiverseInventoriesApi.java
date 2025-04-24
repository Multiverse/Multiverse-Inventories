package org.mvplugins.multiverse.inventories;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.DataImportManager;
import org.mvplugins.multiverse.inventories.profile.PlayerNamesMapper;
import org.mvplugins.multiverse.inventories.profile.ProfileCacheManager;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

import java.util.Objects;

/**
 * Provides access to the Multiverse-Inventories API.
 */
public final class MultiverseInventoriesApi {

    private static MultiverseInventoriesApi instance;

    static void init(@NotNull PluginServiceLocator serviceLocator) {
        if (instance != null) {
            throw new IllegalStateException("MultiverseCoreApi has already been initialized!");
        }
        instance = new MultiverseInventoriesApi(serviceLocator);
    }

    static void shutdown() {
        instance = null;
    }

    /**
     * Gets the MultiverseInventoriesApi. This will throw an exception if the Multiverse-Inventories has not been initialized.
     *
     * @return The MultiverseInventoriesApi
     */
    public static @NotNull MultiverseInventoriesApi get() {
        if (instance == null) {
            throw new IllegalStateException("MultiverseInventoriesApi has not been initialized!");
        }
        return instance;
    }

    private final PluginServiceLocator serviceLocator;

    private MultiverseInventoriesApi(@NotNull PluginServiceLocator serviceProvider) {
        this.serviceLocator = serviceProvider;
    }

    /**
     * Gets instance of our DataImportManager api.
     *
     * @return The DataImportManager instance
     */
    public @NotNull DataImportManager getDataImportManager() {
        return Objects.requireNonNull(serviceLocator.getService(DataImportManager.class));
    }

    /**
     * Gets instance of our InventoriesConfig api.
     *
     * @return The InventoriesConfig instance
     */
    public @NotNull InventoriesConfig getInventoriesConfig() {
        return Objects.requireNonNull(serviceLocator.getService(InventoriesConfig.class));
    }

    /**
     * Gets instance of our PlayerNamesMapper api.
     *
     * @return The PlayerNamesMapper instance
     */
    public @NotNull PlayerNamesMapper getPlayerNamesMapper() {
        return Objects.requireNonNull(serviceLocator.getService(PlayerNamesMapper.class));
    }

    /**
     * Gets instance of our ProfileCacheManager api.
     *
     * @return The ProfileCacheManager instance
     */
    public @NotNull ProfileCacheManager getProfileCacheManager() {
        return Objects.requireNonNull(serviceLocator.getService(ProfileCacheManager.class));
    }

    /**
     * Gets instance of our ProfileContainerStoreProvider api.
     *
     * @return The ProfileContainerStoreProvider instance
     */
    public @NotNull ProfileContainerStoreProvider getProfileContainerStoreProvider() {
        return Objects.requireNonNull(serviceLocator.getService(ProfileContainerStoreProvider.class));
    }

    /**
     * Gets instance of our ProfileDataSource api.
     *
     * @return The ProfileDataSource instance
     */
    public @NotNull ProfileDataSource getProfileDataSource() {
        return Objects.requireNonNull(serviceLocator.getService(ProfileDataSource.class));
    }

    /**
     * Gets instance of our WorldGroupManager api.
     *
     * @return The WorldGroupManager instance
     */
    public @NotNull WorldGroupManager getWorldGroupManager() {
        return Objects.requireNonNull(serviceLocator.getService(WorldGroupManager.class));
    }

    /**
     * Gets the instance of Multiverse-Inventories's PluginServiceLocator.
     * <br/>
     * You can use this to hook into the hk2 dependency injection system used by Multiverse-Inventories.
     *
     * @return The Multiverse-Inventories's PluginServiceLocator
     */
    public @NotNull PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}
