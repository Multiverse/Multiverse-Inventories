package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.util.Logging;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.util.List;

/**
 * PlayerListener for MultiverseInventories.
 */
public class InventoriesListener implements Listener {

    private Inventories inventories;
    private List<WorldGroupProfile> currentGroups;
    private Location spawnLoc = null;

    public InventoriesListener(Inventories inventories) {
        this.inventories = inventories;
    }

    /**
     * Adds Multiverse-Inventories version info to /mv version.
     *
     * @param event The MVVersionEvent that this plugin will listen for.
     */
    @EventHandler
    public void versionRequest(MVVersionEvent event) {
        event.appendVersionInfo(this.inventories.getVersionInfo());
    }

    /**
     * Hooks Multiverse-Inventories into the Multiverse reload command.
     *
     * @param event The MVConfigReloadEvent that this plugin will listen for.
     */
    @EventHandler
    public void configReload(MVConfigReloadEvent event) {
        this.inventories.reloadConfig();
        event.addConfig("Multiverse-Inventories - config.yml");
    }

    /**
     * Called when a plugin is enabled.
     *
     * @param event The plugin enable event.
     */
    @EventHandler
    public void pluginEnable(PluginEnableEvent event) {
        try {
            if (event.getPlugin() instanceof MultiInv) {
                this.inventories.getImportManager().hookMultiInv((MultiInv) event.getPlugin());
            } else if (event.getPlugin() instanceof WorldInventories) {
                this.inventories.getImportManager().hookWorldInventories((WorldInventories) event.getPlugin());
            }
        } catch (NoClassDefFoundError ignore) {
        }
    }

    /**
     * Called when a plugin is disabled.
     *
     * @param event The plugin disable event.
     */
    @EventHandler
    public void pluginDisable(PluginDisableEvent event) {
        try {
            if (event.getPlugin() instanceof MultiInv) {
                this.inventories.getImportManager().unHookMultiInv();
            } else if (event.getPlugin() instanceof WorldInventories) {
                this.inventories.getImportManager().unHookWorldInventories();
            }
        } catch (NoClassDefFoundError ignore) {
        }
    }

    /**
     * Called when a player changes worlds.
     *
     * @param event The world change event.
     */
    @EventHandler
    public void playerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            Logging.fine("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Do nothing if dealing with non-managed worlds
        if (this.inventories.getCore().getMVWorldManager().getMVWorld(toWorld) == null
                || this.inventories.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            Logging.fine("The from or to world is not managed by Multiverse!");
            return;
        }

        new ShareHandler(this.inventories, player, fromWorld, toWorld).handleSharing();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()
                || event.getFrom().getWorld().equals(event.getTo().getWorld())
                || !this.inventories.getMVIConfig().getOptionalShares().contains(Sharables.LAST_LOCATION)) {
            return;
        }
        String fromWorldName = event.getFrom().getWorld().getName();
        String toWorldName = event.getTo().getWorld().getName();
        WorldProfile fromWorldProfile = this.inventories.getWorldManager().getWorldProfile(fromWorldName);
        fromWorldProfile.getPlayerData(event.getPlayer()).set(Sharables.LAST_LOCATION, event.getFrom());
        List<WorldGroupProfile> fromGroups = this.inventories.getGroupManager().getGroupsForWorld(fromWorldName);
        for (WorldGroupProfile fromGroup : fromGroups) {
            if (fromGroup.containsWorld(toWorldName)) {
                if (!fromGroup.isSharing(Sharables.LAST_LOCATION)) {
                    fromGroup.getPlayerData(event.getPlayer()).set(Sharables.LAST_LOCATION, event.getFrom());
                }
            } else {
                fromGroup.getPlayerData(event.getPlayer()).set(Sharables.LAST_LOCATION, event.getFrom());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerDeath(PlayerDeathEvent event) {
        String deathWorld = event.getEntity().getWorld().getName();
        WorldProfile worldProfile = this.inventories.getWorldManager().getWorldProfile(deathWorld);
        PlayerProfile profile = worldProfile.getPlayerData(event.getEntity());
        profile.set(Sharables.LEVEL, event.getNewLevel());
        profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
        profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
        this.inventories.getData().updatePlayerData(worldProfile.getDataName(), profile);
        for (WorldGroupProfile groupProfile : this.inventories.getGroupManager().getGroupsForWorld(deathWorld)) {
            profile = groupProfile.getPlayerData(event.getEntity());
            boolean changed = false;
            if (groupProfile.isSharing(Sharables.LEVEL)) {
                profile.set(Sharables.LEVEL, event.getNewLevel());
                changed = true;
            }
            if (groupProfile.isSharing(Sharables.EXPERIENCE)) {
                profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
                changed = true;
            }
            if (groupProfile.isSharing(Sharables.TOTAL_EXPERIENCE)) {
                profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
                changed = true;
            }
            if (changed) {
                this.inventories.getData().updatePlayerData(groupProfile.getDataName(), profile);
            }
        }
    }

    /**
     * Handles player respawns at the LOWEST priority.
     *
     * @param event The player respawn event.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void lowestPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            World world = event.getPlayer().getWorld();
            this.currentGroups = this.inventories.getGroupManager()
                    .getGroupsForWorld(world.getName());
            this.handleRespawn(event, EventPriority.LOWEST);
        }
    }

    /**
     * Handles player respawns at the LOW priority.
     *
     * @param event The player respawn event.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void lowPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.LOW);
        }
    }

    /**
     * Handles player respawns at the NORMAL priority.
     *
     * @param event The player respawn event.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void normalPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.NORMAL);
        }
    }

    /**
     * Handles player respawns at the HIGH priority.
     *
     * @param event The player respawn event.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void highPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.HIGH);
        }
    }

    /**
     * Handles player respawns at the HIGHEST priority.
     *
     * @param event The player respawn event.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void highestPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.HIGHEST);
        }
    }

    /**
     * Handles player respawns at the MONITOR priority.
     *
     * @param event The player respawn event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorPriorityRespawn(PlayerRespawnEvent event) {
        if (!event.isBedSpawn()) {
            this.handleRespawn(event, EventPriority.MONITOR);
            this.updateCompass(event);
        }
    }

    private void handleRespawn(PlayerRespawnEvent event, EventPriority priority) {
        for (WorldGroupProfile group : this.currentGroups) {
            if (group.getSpawnPriority().equals(priority)) {
                String spawnWorldName = group.getSpawnWorld();
                if (spawnWorldName != null) {
                    MultiverseWorld mvWorld = this.inventories.getCore()
                            .getMVWorldManager().getMVWorld(spawnWorldName);
                    if (mvWorld != null) {
                        this.spawnLoc = mvWorld.getSpawnLocation();
                        event.setRespawnLocation(this.spawnLoc);
                        break;
                    }
                }
            }
        }
    }

    private void updateCompass(PlayerRespawnEvent event) {
        if (event.getRespawnLocation().equals(this.spawnLoc)) {
            event.getPlayer().setCompassTarget(this.spawnLoc);
        }
    }
}

