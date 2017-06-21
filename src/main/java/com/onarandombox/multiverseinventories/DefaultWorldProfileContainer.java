package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.container.WorldProfileContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;

final class DefaultWorldProfileContainer extends WeakProfileContainer implements WorldProfileContainer {

    private final String worldName;

    DefaultWorldProfileContainer(MultiverseInventories inventories, String worldName) {
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

