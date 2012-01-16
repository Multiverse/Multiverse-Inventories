package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.data.MVIData;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Simple implementation of WorldProfile.
 */
public class SimpleWorldProfile extends WeakProfileContainer implements WorldProfile {

    private String worldName;

    public SimpleWorldProfile(MVIData data, String worldName) {
        super(data, ProfileType.WORLD);
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
