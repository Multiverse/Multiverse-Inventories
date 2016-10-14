package com.onarandombox.multiverseinventories.api.profile;

import java.util.UUID;

/**
 * Interface for interacting with the persisted data of this plugin.
 */
public interface PlayerData {

    /**
     * Updates the persisted data for a player for a specific world profile.
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
     * @param playerName Player to retrieve for.
     * @return The player as returned from data.  If no data was found, a new PlayerProfile will be
     *         created.
     */
    @Deprecated
    PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName   World/Group to retrieve from.
     * @param profileType The type of profile to load data for, typically based on game mode.
     * @param playerUUID PlayerUUID to retrieve for.
     * @return The player as returned from data.  If no data was found, a new PlayerProfile will be
     *         created.
     */
    PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID);

    /**
     * Removes the persisted data for a player for a specific world profile.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName   The name of the world/group the player's data is associated with.
     * @param profileType The type of profile we're removing, as per {@link ProfileType}.  If null, this will remove
     *                    remove all profile types.
     * @param playerName The name of the player whose data is being removed.
     * @return True if successfully removed.
     */
    @Deprecated
    boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName);

    /**
     * Removes the persisted data for a player for a specific world profile.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName   The name of the world/group the player's data is associated with.
     * @param profileType The type of profile we're removing, as per {@link ProfileType}.  If null, this will remove
     *                    remove all profile types.
     * @param playerUUID The name of the player whose data is being removed.
     * @return True if successfully removed.
     */
    boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID);

    /**
     * Retrieves the GlobalProfile for a player which contains Multiverse-Inventories meta-data for the player.
     *
     * @param playerName The name of player to retrieve for.
     * @return The global profile for the specified player.
     */
    @Deprecated
    GlobalProfile getGlobalProfile(String playerName);

    /**
     * Retrieves the GlobalProfile for a player which contains Multiverse-Inventories meta-data for the player.
     *
     * @param playerUUID The name of player to retrieve for.
     * @return The global profile for the specified player.
     */
    GlobalProfile getGlobalProfile(UUID playerUUID);
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
    @Deprecated
    void updateWorld(String playerName, String worldName);

    /**
     * A convenience method to update the GlobalProfile of a player with a specified world.
     *
     * @param playerUUID The player whose global profile this will update.
     * @param worldName The world to update the global profile with.
     */
    void updateWorld(UUID playerUUID, String worldName);

    /**
     * A convenience method for setting whether player data should be loaded on login for the specified player.
     *
     * @param playerName The player whose data should be loaded.
     * @param loadOnLogin Whether or not to load on login.
     */
    @Deprecated
    void setLoadOnLogin(String playerName, boolean loadOnLogin);

    /**
     * A convenience method for setting whether player data should be loaded on login for the specified player.
     *
     * @param playerUUID The player whose data should be loaded.
     * @param loadOnLogin Whether or not to load on login.
     */
    void setLoadOnLogin(UUID playerUUID, boolean loadOnLogin);

    //void updateProfileType(String playerName, ProfileType profileType);
}

