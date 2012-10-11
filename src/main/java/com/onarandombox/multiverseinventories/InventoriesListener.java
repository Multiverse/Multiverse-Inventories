package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.util.Logging;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
     * Called when a player joins the server.
     *
     * @param event The player join event.
     */
    @EventHandler
    public void playerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final GlobalProfile globalProfile = inventories.getData().getGlobalProfile(player.getName());
        final String world = globalProfile.getWorld();
        if (inventories.getMVIConfig().usingLoggingSaveLoad() && globalProfile.shouldLoadOnLogin()) {
            ShareHandler.updatePlayer(inventories, player, new DefaultPersistingProfile(Sharables.allOf(),
                    inventories.getWorldManager().getWorldProfile(world).getPlayerData(player)));
        }
        inventories.getData().setLoadOnLogin(player.getName(), false);
        verifyCorrectWorld(player, player.getWorld().getName());
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
        inventories.getData().updateWorld(player.getName(), world);
        if (inventories.getMVIConfig().usingLoggingSaveLoad()) {
            ShareHandler.updateProfile(inventories, player, new DefaultPersistingProfile(Sharables.allOf(),
                    inventories.getWorldManager().getWorldProfile(world).getPlayerData(player)));
            inventories.getData().setLoadOnLogin(player.getName(), true);
        }
    }

    private void verifyCorrectWorld(Player player, String world) {
        GlobalProfile globalProfile = inventories.getData().getGlobalProfile(player.getName());
        if (globalProfile.getWorld() == null) {
            inventories.getData().updateWorld(player.getName(), world);
        } else {
            if (!world.equals(globalProfile.getWorld())) {
                Logging.fine("Player did not spawn in the world they were last reported to be in!");
                new WorldChangeShareHandler(this.inventories, player,
                        globalProfile.getWorld(), world).handleSharing();
                inventories.getData().updateWorld(player.getName(), world);
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
        // Do nothing if dealing with non-managed worlds
        if (this.inventories.getCore().getMVWorldManager().getMVWorld(toWorld) == null
                || this.inventories.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            Logging.fine("The from or to world is not managed by Multiverse!");
            return;
        }

        new WorldChangeShareHandler(this.inventories, player, fromWorld.getName(), toWorld.getName()).handleSharing();
        inventories.getData().updateWorld(player.getName(), toWorld.getName());
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
        WorldProfile fromWorldProfile = this.inventories.getWorldManager().getWorldProfile(fromWorldName);
        PlayerProfile playerProfile = fromWorldProfile.getPlayerData(player);
        playerProfile.set(Sharables.LAST_LOCATION, event.getFrom());
        List<WorldGroupProfile> fromGroups = this.inventories.getGroupManager().getGroupsForWorld(fromWorldName);
        for (WorldGroupProfile fromGroup : fromGroups) {
            playerProfile = fromGroup.getPlayerData(event.getPlayer());
            if (fromGroup.containsWorld(toWorldName)) {
                if (!fromGroup.isSharing(Sharables.LAST_LOCATION)) {
                    playerProfile.set(Sharables.LAST_LOCATION, event.getFrom());
                }
            } else {
                playerProfile.set(Sharables.LAST_LOCATION, event.getFrom());
            }
        }
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
        WorldProfile worldProfile = this.inventories.getWorldManager().getWorldProfile(deathWorld);
        PlayerProfile profile = worldProfile.getPlayerData(event.getEntity());
        profile.set(Sharables.LEVEL, event.getNewLevel());
        profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
        profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
        this.inventories.getData().updatePlayerData(profile);
        for (WorldGroupProfile groupProfile : this.inventories.getGroupManager().getGroupsForWorld(deathWorld)) {
            profile = groupProfile.getPlayerData(event.getEntity());
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
                verifyCorrectWorld(player, player.getWorld().getName());
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

