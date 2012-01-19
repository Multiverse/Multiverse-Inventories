package com.onarandombox.multiverseinventories.profile;

import org.bukkit.OfflinePlayer;

/**
 * An interface for classes containing PlayerProfiles.
 */
public interface ProfileContainer {

    /**
     * @param player Player to get data for.
     * @return The Player profile for the associated player.
     */
    PlayerProfile getPlayerData(OfflinePlayer player);

    /**
     * Adds a player profile to this world profile.
     *
     * @param playerProfile Player profile to add.
     */
    void addPlayerData(PlayerProfile playerProfile);

    /**
     * @return The name to use to look up Data.
     */
    String getDataName();
}

