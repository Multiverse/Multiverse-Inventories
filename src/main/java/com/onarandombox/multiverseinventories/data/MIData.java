package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;

/**
 * Interface for interacting with the data of this plugin.
 */
public interface MIData {

    /**
     * Updates the persisted data for a player profile for a specific world profile.
     *
     * @param worldName     The name of the world the player's data is associated with.
     * @param playerProfile The profile for the player that is being updated.
     * @return True if successfully updated.
     */
    boolean updatePlayerData(String worldName, PlayerProfile playerProfile);

    /**
     * Retrieves a list of the World Profiles from the data file.
     *
     * @return The list of World Profiles from the data.
     */
    //List<WorldProfile> getWorldProfiles();
    PlayerProfile getPlayerData(String worldName, String playerName);
}
