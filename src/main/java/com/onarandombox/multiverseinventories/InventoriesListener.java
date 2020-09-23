package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.multiverseinventories.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.share.Sharables;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.InventoryHolder;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PlayerListener for MultiverseInventories.
 */
public class InventoriesListener implements Listener {

    private MultiverseInventories inventories;
    private List<WorldGroup> currentGroups;
    private Location spawnLoc = null;

    public InventoriesListener(MultiverseInventories inventories) {
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
     * Called when a player joins the server.
     *
     * @param event The player join event.
     */
    @EventHandler
    public void playerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final GlobalProfile globalProfile = inventories.getData().getGlobalProfile(player.getName(), player.getUniqueId());
        final String world = globalProfile.getLastWorld();
        if (inventories.getMVIConfig().usingLoggingSaveLoad() && globalProfile.shouldLoadOnLogin()) {
            ShareHandler.updatePlayer(inventories, player, new DefaultPersistingProfile(Sharables.allOf(),
                    inventories.getWorldProfileContainerStore().getContainer(world).getPlayerData(player)));
        }
        inventories.getData().setLoadOnLogin(player.getName(), false);
        verifyCorrectWorld(player, player.getWorld().getName(), globalProfile);
    }

    /**
     * Called when a player leaves the server.
     *
     * @param event The player quit event.
     */
    @EventHandler
    public void playerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final String world = event.getPlayer().getWorld().getName();
        inventories.getData().updateLastWorld(player.getName(), world);
        if (inventories.getMVIConfig().usingLoggingSaveLoad()) {
            ShareHandler.updateProfile(inventories, player, new DefaultPersistingProfile(Sharables.allOf(),
                    inventories.getWorldProfileContainerStore().getContainer(world).getPlayerData(player)));
            inventories.getData().setLoadOnLogin(player.getName(), true);
        }
    }

    private void verifyCorrectWorld(Player player, String world, GlobalProfile globalProfile) {
        if (globalProfile.getLastWorld() == null) {
            inventories.getData().updateLastWorld(player.getName(), world);
        } else {
            if (!world.equals(globalProfile.getLastWorld())) {
                Logging.fine("Player did not spawn in the world they were last reported to be in!");
                new WorldChangeShareHandler(this.inventories, player,
                        globalProfile.getLastWorld(), world).handleSharing();
                inventories.getData().updateLastWorld(player.getName(), world);
            }
        }
    }

    /**
     * Called when a player changes game modes.
     *
     * @param event The game mode change event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled() || !inventories.getMVIConfig().isUsingGameModeProfiles()) {
            return;
        }
        Player player = event.getPlayer();
        new GameModeShareHandler(this.inventories, player,
                player.getGameMode(), event.getNewGameMode()).handleSharing();
    }

    /**
     * Called when a player changes worlds.
     *
     * @param event The world change event.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void playerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            Logging.fine("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Warn if not managed by Multiverse-Core
        if (this.inventories.getCore().getMVWorldManager().getMVWorld(toWorld) == null
                || this.inventories.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            Logging.fine("The from or to world is not managed by Multiverse-Core!");
        }

        new WorldChangeShareHandler(this.inventories, player, fromWorld.getName(), toWorld.getName()).handleSharing();
        inventories.getData().updateLastWorld(player.getName(), toWorld.getName());
    }

    /**
     * Called when a player teleports.
     *
     * @param event The player teleport event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()
                || event.getFrom().getWorld().equals(event.getTo().getWorld())
                || !this.inventories.getMVIConfig().getOptionalShares().contains(Sharables.LAST_LOCATION)) {
            return;
        }
        Player player = event.getPlayer();
        String fromWorldName = event.getFrom().getWorld().getName();
        String toWorldName = event.getTo().getWorld().getName();
        ProfileContainer fromWorldProfileContainer = this.inventories.getWorldProfileContainerStore().getContainer(fromWorldName);
        PlayerProfile playerProfile = fromWorldProfileContainer.getPlayerData(player);
        playerProfile.set(Sharables.LAST_LOCATION, event.getFrom());
        List<WorldGroup> fromGroups = this.inventories.getGroupManager().getGroupsForWorld(fromWorldName);
        for (WorldGroup fromGroup : fromGroups) {
            playerProfile = fromGroup.getGroupProfileContainer().getPlayerData(event.getPlayer());
            if (fromGroup.containsWorld(toWorldName)) {
                if (!fromGroup.isSharing(Sharables.LAST_LOCATION)) {
                    playerProfile.set(Sharables.LAST_LOCATION, event.getFrom());
                }
            } else {
                playerProfile.set(Sharables.LAST_LOCATION, event.getFrom());
            }
        }

        // Possibly prevents item duping exploit
        player.closeInventory();
    }

    /**
     * Called when a player dies.
     *
     * @param event The player death event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerDeath(PlayerDeathEvent event) {
        Logging.finer("=== Handling PlayerDeathEvent for: " + event.getEntity().getName() + " ===");
        String deathWorld = event.getEntity().getWorld().getName();
        ProfileContainer worldProfileContainer = this.inventories.getWorldProfileContainerStore().getContainer(deathWorld);
        PlayerProfile profile = worldProfileContainer.getPlayerData(event.getEntity());
        profile.set(Sharables.LEVEL, event.getNewLevel());
        profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
        profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
        this.inventories.getData().updatePlayerData(profile);
        for (WorldGroup worldGroup : this.inventories.getGroupManager().getGroupsForWorld(deathWorld)) {
            profile = worldGroup.getGroupProfileContainer().getPlayerData(event.getEntity());
            profile.set(Sharables.LEVEL, event.getNewLevel());
            profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
            profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
            this.inventories.getData().updatePlayerData(profile);
        }
        Logging.finer("=== Finished handling PlayerDeathEvent for: " + event.getEntity().getName() + "! ===");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerRespawn(PlayerRespawnEvent event) {
        Location respawnLoc = event.getRespawnLocation();
        if (respawnLoc == null) {
            // This probably only happens if a naughty plugin sets the location to null...
            return;
        }
        final Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(inventories, new Runnable() {
            public void run() {
                verifyCorrectWorld(player, player.getWorld().getName(),
                        inventories.getData().getGlobalProfile(player.getName(), player.getUniqueId()));
            }
        }, 2L);
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
        for (WorldGroup group : this.currentGroups) {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Item) && !(entity instanceof InventoryHolder)) {
            return;
        }

        World fromWorld = event.getFrom().getWorld();
        Location toLocation = event.getTo();
        if (toLocation == null) {
            // Apparently this happens sometimes.
            Logging.fine("Entity %s attempted to go to null location", entity);
            return;
        }
        World toWorld = toLocation.getWorld();
        if (toWorld == null) {
            // Apparently this also happens sometimes.
            Logging.fine("Entity %s attempted to go to null world", entity);
            return;
        }

        if (fromWorld.equals(toWorld)) {
            return;
        }

        List<WorldGroup> fromGroups = inventories.getGroupManager().getGroupsForWorld(fromWorld.getName());
        List<WorldGroup> toGroups = inventories.getGroupManager().getGroupsForWorld(toWorld.getName());
        // We only care about the groups that have the inventory sharable
        fromGroups = fromGroups.stream().filter(it -> it.isSharing(Sharables.INVENTORY)).collect(Collectors.toList());
        toGroups = toGroups.stream().filter(it -> it.isSharing(Sharables.INVENTORY)).collect(Collectors.toList());
        for (WorldGroup fromGroup : fromGroups) {
            if (toGroups.contains(fromGroup)) {
                Logging.finest("Allowing item or inventory holding %s to go from world %s to world %s", entity,
                        fromWorld.getName(), toWorld.getName());
                // The from and to destinations share at least one group that has the inventory sharable.
                return;
            }
        }

        Logging.finest("Disallowing item or inventory holding %s to go from world %s to world %s since these" +
                        "worlds do not share inventories", entity, fromWorld.getName(), toWorld.getName());
        event.setCancelled(true);
    }
}

