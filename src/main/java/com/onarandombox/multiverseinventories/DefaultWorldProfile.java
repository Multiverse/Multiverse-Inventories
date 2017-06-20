package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.container.WorldProfile;
import org.bukkit.Bukkit;
import org.bukkit.World;

final class DefaultWorldProfile extends WeakProfileContainer implements WorldProfile {

    private final String worldName;

    DefaultWorldProfile(MultiverseInventories inventories, String worldName) {
        super(inventories);
        this.worldName = worldName;
    }

    @Override
    public World getBukkitWorld() {
        return Bukkit.getWorld(this.getWorld());
    }

    @Override
    public String getWorld() {
        return this.worldName;
    }

    @Override
    public String getContainerName() {
        return this.getWorld();
    }
}

