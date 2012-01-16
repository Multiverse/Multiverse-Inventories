package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileType;

/**
 * Interface for interacting with the data of this plugin.
 */
public interface MVIData {

    /**
     * Updates the persisted data for a player profile for a specific world profile.
     *
     * @param dataName The name of the world/group the player's data is associated with.
     * @param playerProfile The profile for the player that is being updated.
     * @return True if successfully updated.
     */
    boolean updatePlayerData(String dataName, PlayerProfile playerProfile);

    /**
     * Retrieves a PlayerProfile from the data source.
     *
     * @param type The type of profile we're getting.
     * @param dataName World/Group to retrieve from.
     * @param playerName Player to retrieve for.
     * @return The profile as returned from data.  If no data was found, a new PlayerProfile will be
     * created.
     */
    PlayerProfile getPlayerData(ProfileType type, String dataName, String playerName);
}
