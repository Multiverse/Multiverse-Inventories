package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.event.MVConfigReloadEvent;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.GlobalProfile;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
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
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Events related to handling of player profile changes.
 */
@Service
public final class ShareHandleListener implements Listener {

    private final MultiverseInventories inventories;
    private final InventoriesConfig config;
    private final WorldManager worldManager;
    private final WorldGroupManager worldGroupManager;
    private final ProfileDataSource profileDataSource;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;

    @Inject
    ShareHandleListener(
            @NotNull MultiverseInventories inventories, InventoriesConfig config,
            @NotNull WorldManager worldManager,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull ProfileDataSource profileDataSource,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider) {
        this.inventories = inventories;
        this.config = config;
        this.worldManager = worldManager;
        this.worldGroupManager = worldGroupManager;
        this.profileDataSource = profileDataSource;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
    }

    /**
     * Adds Multiverse-Inventories version info to /mv version.
     *
     * @param event The MVVersionEvent that this plugin will listen for.
     */
    @EventHandler
    void dumpsDebugInfoRequest(MVDumpsDebugInfoEvent event) {
        event.appendDebugInfo(getDebugInfo());
        File configFile = new File(this.inventories.getDataFolder(), "config.yml");
        File groupsFile = new File(this.inventories.getDataFolder(), "groups.yml");
        event.putDetailedDebugInfo("multiverse-inventories/config.yml", configFile);
        event.putDetailedDebugInfo("multiverse-inventories/groups.yml", groupsFile);
    }

    /**
     * Builds a String containing Multiverse-Inventories' version info.
     *
     * @return The version info.
     */
    private String getDebugInfo() {
        StringBuilder versionInfo = new StringBuilder("[Multiverse-Inventories] Multiverse-Inventories Version: " + inventories.getDescription().getVersion() + '\n'
                + "[Multiverse-Inventories] === Settings ===" + '\n'
                + "[Multiverse-Inventories] First Run: " + config.isFirstRun() + '\n'
                + "[Multiverse-Inventories] Using Bypass: " + config.isUsingBypass() + '\n'
                + "[Multiverse-Inventories] Default Ungrouped Worlds: " + config.isDefaultingUngroupedWorlds() + '\n'
                + "[Multiverse-Inventories] Save and Load on Log In and Out: " + config.usingLoggingSaveLoad() + '\n'
                + "[Multiverse-Inventories] Using GameMode Profiles: " + config.isUsingGameModeProfiles() + '\n'
                + "[Multiverse-Inventories] === Shares ===" + '\n'
                + "[Multiverse-Inventories] Optionals for Ungrouped Worlds: " + config.usingOptionalsForUngrouped() + '\n'
                + "[Multiverse-Inventories] Enabled Optionals: " + config.getOptionalShares() + '\n'
                + "[Multiverse-Inventories] === Groups ===" + '\n');

        for (WorldGroup group : worldGroupManager.getGroups()) {
            versionInfo.append("[Multiverse-Inventories] ").append(group.toString()).append('\n');
        }

        return versionInfo.toString();
    }

    @EventHandler
    void onDebugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }

    /**
     * Hooks Multiverse-Inventories into the Multiverse reload command.
     *
     * @param event The MVConfigReloadEvent that this plugin will listen for.
     */
    @EventHandler
    void configReload(MVConfigReloadEvent event) {
        this.inventories.reloadConfig();
        event.addConfig("Multiverse-Inventories - config.yml");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void playerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != Result.ALLOWED) {
            return;
        }
        Logging.finer("Loading global profile for Player{name:'%s', uuid:'%s'}.",
                event.getName(), event.getUniqueId());
        verifyCorrectPlayerName(event.getUniqueId(), event.getName());
    }

    /**
     * Called when a player joins the server.
     *
     * @param event The player join event.
     */
    @EventHandler
    void playerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        // Just in case AsyncPlayerPreLoginEvent was still the old name
        verifyCorrectPlayerName(player.getUniqueId(), player.getName());

        final GlobalProfile globalProfile = profileDataSource.getGlobalProfile(player);
        final String world = globalProfile.getLastWorld();
        if (config.usingLoggingSaveLoad() && globalProfile.shouldLoadOnLogin()) {
            ShareHandlingUpdater.updatePlayer(inventories, player, new PersistingProfile(
                    Sharables.allOf(),
                    profileContainerStoreProvider.getStore(ContainerType.WORLD)
                            .getContainer(world)
                            .getPlayerData(player)
            ));
        }
        globalProfile.setLoadOnLogin(false);
        verifyCorrectWorld(player, player.getWorld().getName(), globalProfile);
        profileDataSource.updateGlobalProfile(globalProfile);
    }

    private void verifyCorrectPlayerName(UUID uuid, String name) {
        profileDataSource.getExistingGlobalProfile(uuid, name).peek(globalProfile -> {
            if (globalProfile.getLastKnownName().equals(name)) {
                return;
            }

            // Data must be migrated
            Logging.info("Player %s changed name from '%s' to '%s'. Attempting to migrate playerdata...",
                    uuid, globalProfile.getLastKnownName(), name);
            try {
                profileDataSource.migratePlayerData(globalProfile.getLastKnownName(), name, uuid);
            } catch (IOException e) {
                Logging.severe("An error occurred while trying to migrate playerdata.");
                e.printStackTrace();
            }
            globalProfile.setLastKnownName(name);
            profileDataSource.updateGlobalProfile(globalProfile);
            Logging.info("Migration complete!");
        });
    }

    /**
     * Called when a player leaves the server.
     *
     * @param event The player quit event.
     */
    @EventHandler
    void playerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final String world = event.getPlayer().getWorld().getName();
        GlobalProfile globalProfile = profileDataSource.getGlobalProfile(player);
        globalProfile.setLastWorld(world);
        if (config.usingLoggingSaveLoad()) {
            ShareHandlingUpdater.updateProfile(inventories, player, new PersistingProfile(
                    Sharables.allOf(),
                    profileContainerStoreProvider.getStore(ContainerType.WORLD)
                            .getContainer(world)
                            .getPlayerData(player)
            ));
            globalProfile.setLoadOnLogin(true);
        }
        profileDataSource.updateGlobalProfile(globalProfile);
        SingleShareWriter.of(this.inventories, player, Sharables.LAST_LOCATION).write(player.getLocation().clone());
    }

    private void verifyCorrectWorld(Player player, String world, GlobalProfile globalProfile) {
        if (globalProfile.getLastWorld() == null) {
            globalProfile.setLastWorld(world);
        } else {
            if (!world.equals(globalProfile.getLastWorld())) {
                Logging.fine("Player did not spawn in the world they were last reported to be in!");
                new WorldChangeShareHandler(this.inventories, player,
                        globalProfile.getLastWorld(), world).handleSharing();
                globalProfile.setLastWorld(world);
            }
        }
    }

    /**
     * Called when a player changes game modes.
     *
     * @param event The game mode change event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void playerGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.isCancelled() || !config.isUsingGameModeProfiles()) {
            return;
        }
        Player player = event.getPlayer();
        SingleShareWriter.of(this.inventories, player, Sharables.LAST_LOCATION).write(player.getLocation().clone());
        new GameModeShareHandler(this.inventories, player,
                player.getGameMode(), event.getNewGameMode()).handleSharing();
    }

    /**
     * Called when a player changes worlds.
     *
     * @param event The world change event.
     */
    @EventHandler(priority = EventPriority.LOW)
    void playerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            Logging.fine("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Warn if not managed by Multiverse-Core
        if (!this.worldManager.isLoadedWorld(toWorld) || !this.worldManager.isLoadedWorld(fromWorld)) {
            Logging.fine("The from or to world is not managed by Multiverse-Core!");
        }

        long startTime = System.nanoTime();
        new WorldChangeShareHandler(this.inventories, player, fromWorld.getName(), toWorld.getName()).handleSharing();
        profileDataSource.modifyGlobalProfile(player, profile -> profile.setLastWorld(toWorld.getName()));
        Logging.finest("WorldChangeShareHandler took " + (System.nanoTime() - startTime) / 1000000 + " ms.");
    }

    /**
     * Called when a player teleports.
     *
     * @param event The player teleport event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void playerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()
                || event.getFrom().getWorld().equals(event.getTo().getWorld())
                || !config.getOptionalShares().contains(Sharables.LAST_LOCATION)) {
            return;
        }

        Player player = event.getPlayer();
        SingleShareWriter.of(this.inventories, player, Sharables.LAST_LOCATION).write(event.getFrom().clone());

        // Possibly prevents item duping exploit
        player.closeInventory();
    }

    /**
     * Called when a player dies.
     *
     * @param event The player death event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    void playerDeath(PlayerDeathEvent event) {
        Logging.finer("=== Handling PlayerDeathEvent for: " + event.getEntity().getName() + " ===");
        String deathWorld = event.getEntity().getWorld().getName();
        ProfileContainer worldProfileContainer = profileContainerStoreProvider.getStore(ContainerType.WORLD).getContainer(deathWorld);
        PlayerProfile profile = worldProfileContainer.getPlayerData(event.getEntity());
        profile.set(Sharables.LEVEL, event.getNewLevel());
        profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
        profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
        profileDataSource.updatePlayerData(profile);
        for (WorldGroup worldGroup : worldGroupManager.getGroupsForWorld(deathWorld)) {
            profile = worldGroup.getGroupProfileContainer().getPlayerData(event.getEntity());
            profile.set(Sharables.LEVEL, event.getNewLevel());
            profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
            profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
            profileDataSource.updatePlayerData(profile);
        }
        Logging.finer("=== Finished handling PlayerDeathEvent for: " + event.getEntity().getName() + "! ===");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void playerRespawn(PlayerRespawnEvent event) {
        Location respawnLoc = event.getRespawnLocation();
        if (respawnLoc == null) {
            // This probably only happens if a naughty plugin sets the location to null...
            return;
        }
        final Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(
                inventories,
                () -> verifyCorrectWorld(
                        player,
                        player.getWorld().getName(),
                        profileDataSource.getGlobalProfile(player)),
                2L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void entityPortal(EntityPortalEvent event) {
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

        List<WorldGroup> fromGroups = worldGroupManager.getGroupsForWorld(fromWorld.getName());
        List<WorldGroup> toGroups = worldGroupManager.getGroupsForWorld(toWorld.getName());
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

    @EventHandler
    void worldUnload(WorldUnloadEvent event) {
        String unloadWorldName = event.getWorld().getName();

        Logging.finer("Clearing data for world/groups container with '%s' world.", unloadWorldName);

        ProfileContainer fromWorldProfileContainer = profileContainerStoreProvider.getStore(ContainerType.WORLD)
                .getContainer(unloadWorldName);
        fromWorldProfileContainer.clearContainer();

        List<WorldGroup> fromGroups = worldGroupManager.getGroupsForWorld(unloadWorldName);
        for (WorldGroup fromGroup : fromGroups) {
            fromGroup.getGroupProfileContainer().clearContainer();
        }
    }
}
