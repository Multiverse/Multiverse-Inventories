package com.onarandombox.multiverseinventories.profile.container;

import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * A container for player profiles in a given world or world group (based on {@link #getContainerType()}).
 * <p>Players may have separate profiles per game mode within this container if game mode profiles are enabled.</p>
 */
public interface ProfileContainer {

    /**
     * Returns the name of this profile container which is primarily used for persistence purposes.
     * <p>The name reflects the world name if this is a world profile container, or the arbitrary group name if
     * this is a world group profile container.</p>
     *
     * @return The name to use to look up Data.
     */
    String getContainerName();

    /**
     * Returns the container type for this container.
     *
     * @return the container type.
     */
    ContainerType getContainerType();

    /**
     * Retrieves the profile for the given player.
     * <p>If game mode profiles are enabled, the profile for their current game mode will be returned, otherwise their
     * survival profile will be returned.</p>
     *
     * @param player Player to get profile for.
     * @return The profile for the given player.
     */
    PlayerProfile getPlayerData(Player player);

    /**
     * Retrieves the profile of the given type for the given player.
     *
     * @param profileType The type of profile to get data for, typically Survival or Creative.
     * @param player Player to get profile for.
     * @return The profile of the given type for the given player.
     */
    PlayerProfile getPlayerData(ProfileType profileType, OfflinePlayer player);

    /**
     * Adds a player profile to this profile container.
     *
     * @param playerProfile Player player to add.
     */
    void addPlayerData(PlayerProfile playerProfile);

    /**
     * Removes all of the profile data for a given player in this profile container.
     *
     * @param player Player to remove data for.
     */
    void removeAllPlayerData(OfflinePlayer player);

    /**
     * Removes the profile data for a specific type of profile in this profile container.
     *
     * @param profileType The type of profile to remove data for.
     * @param player Player to remove data for.
     */
    void removePlayerData(ProfileType profileType, OfflinePlayer player);
}

