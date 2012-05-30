package com.onarandombox.multiverseinventories.api.profile;

/**
 * Interface for interacting with the persisted data of this plugin.
 */
public interface PlayerData {

    /**
     * Updates the persisted data for a player for a specific world profile.
     *
     * @param playerProfile The profile for the player that is being updated.
     * @return True if successfully updated.
     */
    boolean updatePlayerData(PlayerProfile playerProfile);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param containerType The type of container this profile is part of, world or group.
     * @param dataName   World/Group to retrieve from.
     * @param playerName Player to retrieve for.
     * @return The player as returned from data.  If no data was found, a new PlayerProfile will be
     *         created.
     */
    PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName);

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
    boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName);

    GlobalProfile getGlobalProfile(String playerName);

    boolean updateGlobalProfile(String playerName, GlobalProfile globalProfile);

    void updateWorld(String playerName, String worldName);

    //void updateProfileType(String playerName, ProfileType profileType);
}

