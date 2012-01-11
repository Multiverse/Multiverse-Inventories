package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.WorldProfile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Interface for interacting with the data of this plugin.
 */
public interface MIData {

    /**
     * Updates the persisted data for a player profile for a specific world profile.
     *
     * @param worldProfile The profile for the world the player's data is associated with.
     * @param playerProfile The profile for the player that is being updated.
     * @return True if successfully updated.
     */
    boolean updatePlayerData(WorldProfile worldProfile, PlayerProfile playerProfile);

    /**
     * Retrieves a list of the World Profiles from the data file.
     *
     * @return The list of World Profiles from the data.
     */
    List<WorldProfile> getWorldProfiles();
}
