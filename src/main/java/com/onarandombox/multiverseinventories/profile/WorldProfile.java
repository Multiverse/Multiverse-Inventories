package com.onarandombox.multiverseinventories.profile;

import org.bukkit.World;

/**
 * Interface for a World Profile which contains all the player profiles for a world.
 */
public interface WorldProfile extends ProfileContainer {

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
}

