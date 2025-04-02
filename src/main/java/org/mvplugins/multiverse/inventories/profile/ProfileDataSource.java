package org.mvplugins.multiverse.inventories.profile;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A source for updating and retrieving player profiles via persistence.
 */
@Contract
public sealed interface ProfileDataSource permits FlatFileProfileDataSource {

    /**
     * Updates the persisted data for a player for a specific profile.
     *
     *
     * @param playerProfile The profile for the player that is being updated.
     */
    CompletableFuture<Void> updatePlayerData(PlayerProfile playerProfile);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param profileKey The key of the profile to retrieve.
     * @return The player as returned from data. If no data was found, a new PlayerProfile will be
     *         created.
     */
    PlayerProfile getPlayerDataNow(ProfileKey profileKey);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param profileKey The key of the profile to retrieve.
     * @return The player as returned from data. If no data was found, a new PlayerProfile will be
     *         created.
     */
    CompletableFuture<PlayerProfile> getPlayerData(ProfileKey profileKey);

    /**
     * Removes the persisted data for a player for a specific profile.
     *
     * @param profileKey The key of the profile to remove.
     * @return True if successfully removed.
     */
    CompletableFuture<Void> removePlayerData(ProfileKey profileKey);

    /**
     * Copies all the data belonging to oldName to newName and removes the old data.
     *
     * @param oldName the previous name of the player.
     * @param newName the new name of the player.
     * @param playerUUID the UUID of the player.
     * @throws IOException Thrown if something goes wrong while migrating the files.
     */
    void migratePlayerData(String oldName, String newName, UUID playerUUID) throws IOException;

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     *
     * @param playerUUID    The UUID of the player.
     * @return The global profile for the specified player.
     */
    @NotNull GlobalProfile getGlobalProfileNow(UUID playerUUID);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     *
     * @param player    The player.
     * @return The global profile for the specified player.
     */
    @NotNull GlobalProfile getGlobalProfileNow(OfflinePlayer player);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     * Creates the profile if it doesn't exist.
     *
     * @param playerUUID    The UUID of the player.
     * @param playerName    The name of the player.
     * @return The global profile for the specified player.
     */
    @NotNull GlobalProfile getGlobalProfileNow(UUID playerUUID, String playerName);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player if it exists.
     *
     * @param playerUUID The UUID of the player.
     * @param playerName The name of the player.
     * @return The global profile for the specified player or empty if it doesn't exist.
     */
    @NotNull Option<GlobalProfile> getExistingGlobalProfileNow(UUID playerUUID, String playerName);

    CompletableFuture<GlobalProfile> getGlobalProfile(UUID playerUUID);

    CompletableFuture<GlobalProfile> getGlobalProfile(OfflinePlayer player);

    @NotNull CompletableFuture<GlobalProfile> getGlobalProfile(UUID playerUUID, String playerName);

    @NotNull CompletableFuture<Option<GlobalProfile>> getExistingGlobalProfile(UUID playerUUID, String playerName);

    CompletableFuture<Void> modifyGlobalProfile(UUID playerUUID, Consumer<GlobalProfile> consumer);

    CompletableFuture<Void> modifyGlobalProfile(OfflinePlayer offlinePlayer, Consumer<GlobalProfile> consumer);

    /**
     * Update the file for a player's global profile.
     *
     * @param globalProfile The GlobalProfile object to update the file for.
     */
    CompletableFuture<Void> updateGlobalProfile(GlobalProfile globalProfile);

    /**
     * Clears a single profile in cache.
     */
    void clearProfileCache(ProfileKey key);

    /**
     * Clears all profiles in cache that match the predicate.
     */
    void clearProfileCache(Predicate<ProfileKey> predicate);

    /**
     * Clears all profiles in cache.
     */
    void clearAllCache();

    /**
     * Gets the cache stats for the profile data source.
     *
     * @return The cache stats.
     */
    Map<String, CacheStats> getCacheStats();

    Collection<UUID> getGlobalPlayersList();

    Collection<String> getContainerPlayersList(ContainerType containerType, String containerName);

    Collection<String> getContainerNames(ContainerType containerType);
}

