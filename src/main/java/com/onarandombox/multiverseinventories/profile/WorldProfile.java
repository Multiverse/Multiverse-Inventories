package com.onarandombox.multiverseinventories.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

/**
 * Interface for a World Profile which contains all the player profiles for a world.
 */
public interface WorldProfile {

    /**
     * @return The world associated with this profile.
     */
    World getBukkitWorld();

    /**
     * @return The name of the world associated with this profile.
     */
    String getWorld();

    /**
     * Sets the world this profile is associated with.
     *
     * @param worldName Name of world to associate this profile with.
     */
    void setWorld(String worldName);

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
}
