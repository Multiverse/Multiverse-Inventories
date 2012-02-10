package com.onarandombox.multiverseinventories.api.profile;

import org.bukkit.OfflinePlayer;

/**
 * An interface for classes containing PlayerProfiles.
 */
public interface ProfileContainer {

    /**
     * @param player Player to get data for.
     * @return The Player player for the associated player.
     */
    PlayerProfile getPlayerData(OfflinePlayer player);

    /**
     * Adds a player player to this world player.
     *
     * @param playerProfile Player player to add.
     */
    void addPlayerData(PlayerProfile playerProfile);

    /**
     * @return The name to use to look up Data.
     */
    String getDataName();
}

