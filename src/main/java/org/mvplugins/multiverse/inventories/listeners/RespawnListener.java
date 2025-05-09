package org.mvplugins.multiverse.inventories.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

import java.util.List;

/**
 * Specific events for handling player respawns location
 */
@Service
final class RespawnListener implements MVInvListener {

    private final WorldGroupManager worldGroupManager;
    private final WorldManager worldManager;

    private List<WorldGroup> currentGroups;
    private Location spawnLoc = null;

    @Inject
    RespawnListener(WorldGroupManager worldGroupManager, WorldManager worldManager) {
        this.worldGroupManager = worldGroupManager;
        this.worldManager = worldManager;
    }

    /**
     * Handles player respawns at the LOWEST priority.
     *
     * @param event The player respawn event.
     */
    @EventMethod
    @DefaultEventPriority(EventPriority.LOWEST)
    void lowestPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            World world = event.getPlayer().getWorld();
            this.currentGroups = worldGroupManager.getGroupsForWorld(world.getName());
            this.handleRespawn(event, EventPriority.LOWEST);
        }
    }

    /**
     * Handles player respawns at the LOW priority.
     *
     * @param event The player respawn event.
     */
    @EventMethod
    @DefaultEventPriority(EventPriority.LOW)
    void lowPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.LOW);
        }
    }

    /**
     * Handles player respawns at the NORMAL priority.
     *
     * @param event The player respawn event.
     */
    @EventMethod
    @DefaultEventPriority(EventPriority.NORMAL)
    void normalPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.NORMAL);
        }
    }

    /**
     * Handles player respawns at the HIGH priority.
     *
     * @param event The player respawn event.
     */
    @EventMethod
    @DefaultEventPriority(EventPriority.HIGH)
    void highPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.HIGH);
        }
    }

    /**
     * Handles player respawns at the HIGHEST priority.
     *
     * @param event The player respawn event.
     */
    @EventMethod
    @DefaultEventPriority(EventPriority.HIGHEST)
    void highestPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.HIGHEST);
        }
    }

    /**
     * Handles player respawns at the MONITOR priority.
     *
     * @param event The player respawn event.
     */
    @EventMethod
    @DefaultEventPriority(EventPriority.MONITOR)
    void monitorPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.MONITOR);
            this.updateCompass(event);
        }
    }

    private void handleRespawn(PlayerRespawnEvent event, EventPriority priority) {
        for (WorldGroup group : this.currentGroups) {
            if (!group.getSpawnPriority().equals(priority)) {
                continue;
            }
            String spawnWorldName = group.getSpawnWorld();
            if (spawnWorldName == null) {
                continue;
            }
            LoadedMultiverseWorld mvWorld = worldManager.getLoadedWorld(spawnWorldName).getOrNull();
            if (mvWorld != null) {
                this.spawnLoc = mvWorld.getSpawnLocation();
                event.setRespawnLocation(this.spawnLoc);
                break;
            }
        }
    }

    private void updateCompass(PlayerRespawnEvent event) {
        if (event.getRespawnLocation().equals(this.spawnLoc)) {
            event.getPlayer().setCompassTarget(this.spawnLoc);
        }
    }
}
