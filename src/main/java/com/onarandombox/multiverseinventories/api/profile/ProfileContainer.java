package com.onarandombox.multiverseinventories.api.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * An interface for classes containing PlayerProfiles.  This describes where to retrieve a player's
 * data from persistence via {@link #getDataName()} and allows data to be retrieved, added and removed
 * via the other methods.
 */
public interface ProfileContainer {

    /**
     * @param player Player to get data for.
     * @return The Player profile for the associated player for the {@link ProfileType} for their current GameMode if
     * {@link com.onarandombox.multiverseinventories.api.InventoriesConfig#isUsingGameModeProfiles()} is true or for
     * {@link com.onarandombox.multiverseinventories.ProfileTypes#SURVIVAL} otherwise.
     */
    PlayerProfile getPlayerData(Player player);

    /**
     * @param profileType The type of profile to get data for, typically Survival or Creative.
     * @param player Player to get data for.
     * @return The Player profile for the associated player for the specified {@link ProfileType}.
     */
    PlayerProfile getPlayerData(ProfileType profileType, OfflinePlayer player);

    /**
     * Adds a player profile to this world player.
     *
     * @param playerProfile Player player to add.
     */
    void addPlayerData(PlayerProfile playerProfile);

    /**
     * Removes all of the profile data for a given player in this world/group.
     *
     * @param player Player to remove data for.
     */
    void removeAllPlayerData(OfflinePlayer player);

    /**
     * Removes the profile data for a specific type of profile in this world/group.
     *
     * @param profileType The type of profile to remove data for.
     * @param player Player to remove data for.
     */
    void removePlayerData(ProfileType profileType, OfflinePlayer player);

    /**
     * @return The name to use to look up Data.
     */
    String getDataName();
}

