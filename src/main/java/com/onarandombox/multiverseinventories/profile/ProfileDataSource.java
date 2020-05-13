package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.UUID;

/**
 * A source for updating and retrieving player profiles via persistence.
 */
public interface ProfileDataSource {

    /**
     * Updates the persisted data for a player for a specific profile.
     *
     * @param playerProfile The profile for the player that is being updated.
     */
    void updatePlayerData(PlayerProfile playerProfile);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName World/Group to retrieve from.
     * @param profileType The type of profile to load data for, typically based on game mode.
     * @param player The OfflinePlayer to retrieve for.
     * @return The player as returned from data. If no data was found, a new PlayerProfile will be
     *         created.
     */
    PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, OfflinePlayer player);

    /**
     * Removes the persisted data for a player for a specific profile.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName The name of the world/group the player's data is associated with.
     * @param profileType The type of profile we're removing, as per {@link ProfileType}. If null, this will remove
     *                    remove all profile types.
     * @param player The OfflinePlayer whose data is being removed.
     * @return True if successfully removed.
     */
    boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, OfflinePlayer player);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     *
     * @param playerName The name of player to retrieve for.
     * @return The global profile for the specified player.
     * @deprecated UUID must be supported now.
     */
    @Deprecated
    GlobalProfile getGlobalProfile(String playerName);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     *
     * @param playerName The name of the player to retrieve for. This is required for updating name last known as.
     * @param playerUUID The UUID of the player.
     * @return the global profile for the player with the given UUID.
     */
    GlobalProfile getGlobalProfile(String playerName, UUID playerUUID);

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
     * @param playerName The player whose global profile this will update.
     * @param worldName The world to update the global profile with.
     */
    void updateLastWorld(String playerName, String worldName);

    /**
     * A convenience method for setting whether player data should be loaded on login for the specified player.
     *
     * @param playerName The player whose data should be loaded.
     * @param loadOnLogin Whether or not to load on login.
     */
    void setLoadOnLogin(String playerName, boolean loadOnLogin);

    /**
     * Copies all the data belonging to oldName to newName and removes the old data.
     *
     * @param oldPlayer The old OfflinePlayer.
     * @param newPlayer The OfflineProfile we will be migrating data to.
     * @param removeOldData Whether or not to remove the data belonging to oldName.
     * @throws IOException Thrown if something goes wrong while migrating the files.
     */
    void migratePlayerData(OfflinePlayer oldPlayer, OfflinePlayer newPlayer, boolean removeOldData) throws IOException;
}

