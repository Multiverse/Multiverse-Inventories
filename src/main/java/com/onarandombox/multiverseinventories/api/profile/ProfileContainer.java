package com.onarandombox.multiverseinventories.api.profile;

import org.bukkit.OfflinePlayer;

/**
 * An interface for classes containing PlayerProfiles.  This describes where to retrieve a player's
 * data from persistence via {@link #getDataName()} and allows data to be retrieved, added and removed
 * via the other methods.
 */
public interface ProfileContainer {

    /**
     * @param player Player to get data for.
     * @return The Player profile for the associated player for their currently active {@link ProfileType}.
     */
    PlayerProfile getPlayerData(OfflinePlayer player);

    PlayerProfile getPlayerData(ProfileType profileType, OfflinePlayer player);

    /**
     * Adds a player profile to this world player.
     *
     * @param playerProfile Player player to add.
     */
    void addPlayerData(PlayerProfile playerProfile);

    /**
     * Removes the profile data for a given player in this world/group.
     *
     * @param player Player to remove data for.
     */
    void removeAllPlayerData(OfflinePlayer player);

    void removePlayerData(ProfileType profileType, OfflinePlayer player);

    /**
     * @return The name to use to look up Data.
     */
    String getDataName();
}

