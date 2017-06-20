package com.onarandombox.multiverseinventories.profile.container;

import org.bukkit.World;

/**
 * Contains all the player profiles for a world.
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
     * {@inheritDoc}
     */
    @Override
    default ContainerType getContainerType() {
        return ContainerType.WORLD;
    }
}

