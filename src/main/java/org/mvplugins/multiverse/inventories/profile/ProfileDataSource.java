package org.mvplugins.multiverse.inventories.profile;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;

import java.io.IOException;
import java.util.UUID;
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
    void updatePlayerData(PlayerProfile playerProfile);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName   World/Group to retrieve from.
     * @param profileType The type of profile to load data for, typically based on game mode.
     * @param playerUUID UUID of the player to retrieve for.
     * @return The player as returned from data. If no data was found, a new PlayerProfile will be
     *         created.
     */
    PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID);

    /**
     * Removes the persisted data for a player for a specific profile.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName   The name of the world/group the player's data is associated with.
     * @param profileType The type of profile we're removing, as per {@link ProfileType}. If null, this will remove
     *                    remove all profile types.
     * @param playerUUID The UUID of the player whose data is being removed.
     * @return True if successfully removed.
     */
    boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     *
     * @param playerUUID    The UUID of the player.
     * @return The global profile for the specified player.
     */
    GlobalProfile getGlobalProfile(UUID playerUUID);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     *
     * @param player    The player.
     * @return The global profile for the specified player.
     */
    GlobalProfile getGlobalProfile(OfflinePlayer player);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     * Creates the profile if it doesn't exist.
     *
     * @param playerName    The name of the player.
     * @param playerUUID    The UUID of the player.
     * @return The global profile for the specified player.
     */
    @NotNull GlobalProfile getGlobalProfile(String playerName, UUID playerUUID);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player if it exists.
     *
     * @param playerName    The name of the player.
     * @param playerUUID    The UUID of the player.
     * @return The global profile for the specified player or empty if it doesn't exist.
     */
    @NotNull Option<GlobalProfile> getExistingGlobalProfile(String playerName, UUID playerUUID);

    /**
     * Update the file for a player's global profile.
     *
     * @param globalProfile The GlobalProfile object to update the file for.
     * @return True if data successfully saved to file.
     */
    boolean updateGlobalProfile(GlobalProfile globalProfile);

    /**
     * A convenience method to update the GlobalProfile of a player with a specified world.
     *
     * @param playerUUID The player whose global profile this will update.
     * @param worldName The world to update the global profile with.
     */
    void updateLastWorld(UUID playerUUID, String worldName);

    /**
     * A convenience method for setting whether player data should be loaded on login for the specified player.
     *
     * @param playerUUID The player whose data should be loaded.
     * @param loadOnLogin Whether or not to load on login.
     */
    void setLoadOnLogin(UUID playerUUID, boolean loadOnLogin);

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
     * Clears a single profile in cache.
     */
    void clearProfileCache(ProfileKey key);

    /**
     * Clears a single profile in cache.
     */
    void clearProfileCache(Predicate<ProfileKey> predicate);

    /**
     * Clears all profiles in cache.
     */
    void clearAllCache();
}

