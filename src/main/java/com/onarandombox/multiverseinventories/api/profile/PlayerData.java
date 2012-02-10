package com.onarandombox.multiverseinventories.api.profile;

/**
 * Interface for interacting with the persisted data of this plugin.
 */
public interface PlayerData {

    /**
     * Updates the persisted data for a player player for a specific world player.
     *
     * @param dataName      The name of the world/group the player's data is associated with.
     * @param playerProfile The player for the player that is being updated.
     * @return True if successfully updated.
     */
    boolean updatePlayerData(String dataName, PlayerProfile playerProfile);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param type       The type of player we're getting.
     * @param dataName   World/Group to retrieve from.
     * @param playerName Player to retrieve for.
     * @return The player as returned from data.  If no data was found, a new PlayerProfile will be
     *         created.
     */
    PlayerProfile getPlayerData(ProfileType type, String dataName, String playerName);
}

