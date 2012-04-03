package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Simple implementation of WorldProfile.
 */
final class DefaultWorldProfile extends WeakProfileContainer implements WorldProfile {

    private String worldName;

    public DefaultWorldProfile(Inventories inventories, String worldName) {
        super(inventories, ContainerType.WORLD);
        this.worldName = worldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getBukkitWorld() {
        return Bukkit.getWorld(this.getWorld());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorld() {
        return this.worldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorld(String worldName) {
        this.worldName = this.worldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDataName() {
        return this.getWorld();
    }
}

