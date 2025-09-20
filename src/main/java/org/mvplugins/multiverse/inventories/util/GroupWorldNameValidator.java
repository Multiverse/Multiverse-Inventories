package org.mvplugins.multiverse.inventories.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;

@Service
@ApiStatus.Internal
public class GroupWorldNameValidator {

    private final WorldManager worldManager;

    @Inject
    GroupWorldNameValidator(@NotNull WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @ApiStatus.Internal
    public boolean validateWorldName(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            return false;
        }
        // For the new wildcard and regex support
        if (worldName.contains("*") || worldName.startsWith("r=")) {
            return true;
        }
        return worldManager.isWorld(worldName) || Bukkit.getWorld(worldName) != null;
    }
}
