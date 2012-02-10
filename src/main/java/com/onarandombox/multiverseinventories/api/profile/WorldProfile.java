package com.onarandombox.multiverseinventories.api.profile;

import org.bukkit.World;

/**
 * Interface for a World Profile which contains all the player profiles for a world.
 */
public interface WorldProfile extends ProfileContainer {

    /**
     * @return The world associated with this player.
     */
    World getBukkitWorld();

    /**
     * @return The name of the world associated with this player.
     */
    String getWorld();

    /**
     * Sets the world this player is associated with.
     *
     * @param worldName Name of world to associate this player with.
     */
    void setWorld(String worldName);
}

