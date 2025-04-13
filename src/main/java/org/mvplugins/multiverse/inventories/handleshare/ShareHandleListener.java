package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
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
import org.mvplugins.multiverse.inventories.util.FutureNow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    @EventHandler(priority = EventPriority.MONITOR)
    void playerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != Result.ALLOWED) {
            return;
        }
        Logging.finer("Loading global profile for Player{name:'%s', uuid:'%s'}.",
                event.getName(), event.getUniqueId());
        verifyCorrectPlayerName(event.getUniqueId(), event.getName());

        long startTime = System.nanoTime();
        List<CompletableFuture<PlayerProfile>> profileFutures = new ArrayList<>();
        config.getPreloadDataOnJoinWorlds().forEach(worldName -> profileFutures.add(profileDataSource.getPlayerProfile(
                ProfileKey.create(ContainerType.WORLD, worldName, ProfileTypes.SURVIVAL, event.getUniqueId(), event.getName()))));
        config.getPreloadDataOnJoinGroups().forEach(groupName -> profileFutures.add(profileDataSource.getPlayerProfile(
                ProfileKey.create(ContainerType.GROUP, groupName, ProfileTypes.SURVIVAL, event.getUniqueId(), event.getName()))));
        Try.run(() -> CompletableFuture.allOf(profileFutures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS))
                .onSuccess(ignore -> Logging.finer("Preloaded data for Player{name:'%s', uuid:'%s'}. Time taken: %4.4f ms",
                        event.getName(), event.getUniqueId(), (System.nanoTime() - startTime) / 1000000.0))
                .onFailure(e -> Logging.warning("Preload data errored out: %s", e.getMessage()));
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

        final GlobalProfile globalProfile = FutureNow.get(profileDataSource.getGlobalProfile(GlobalProfileKey.create(player)));
        if (globalProfile.shouldLoadOnLogin()) {
            new ReadOnlyShareHandler(inventories, player).handleSharing();
        }
        globalProfile.setLoadOnLogin(false);
        verifyCorrectWorld(player, player.getWorld().getName(), globalProfile);
        profileDataSource.updateGlobalProfile(globalProfile);
    }

    private void verifyCorrectPlayerName(UUID uuid, String name) {
        FutureNow.get(profileDataSource.getExistingGlobalProfile(GlobalProfileKey.create(uuid, name))).peek(globalProfile -> {
            if (globalProfile.getLastKnownName().equals(name)) {
                return;
            }

            // Data must be migrated
            Logging.info("Player %s changed name from '%s' to '%s'. Attempting to migrate playerdata...",
                    uuid, globalProfile.getLastKnownName(), name);
            try {
                profileDataSource.migratePlayerProfileName(globalProfile.getLastKnownName(), name);
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

        CompletableFuture<GlobalProfile> globalProfile = profileDataSource.getGlobalProfile(GlobalProfileKey.create(player));
        globalProfile.thenAccept(p -> p.setLastWorld(world));

        // Write last location as its possible for players to join at a different world
        SingleShareWriter.of(this.inventories, player, Sharables.LAST_LOCATION).write(player.getLocation().clone());
        new WriteOnlyShareHandler(inventories, player).handleSharing();
        if (config.getApplyPlayerdataOnJoin()) {
            globalProfile.thenAccept(p -> p.setLoadOnLogin(true));
        }
        globalProfile.thenAccept(profileDataSource::updateGlobalProfile);
    }

    private void verifyCorrectWorld(Player player, String world, GlobalProfile globalProfile) {
        if (Strings.isNullOrEmpty(globalProfile.getLastWorld())) {
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
        if (event.isCancelled() || !config.getEnableGamemodeShareHandling()) {
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

        new WorldChangeShareHandler(this.inventories, player, fromWorld.getName(), toWorld.getName()).handleSharing();
        profileDataSource.modifyGlobalProfile(
                GlobalProfileKey.create(player), profile -> profile.setLastWorld(toWorld.getName()));
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
                || !config.getActiveOptionalShares().contains(Sharables.LAST_LOCATION)) {
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
        PlayerProfile profile = worldProfileContainer.getPlayerProfileNow(event.getEntity());
        resetStatsOnDeath(event, profile);
        for (WorldGroup worldGroup : worldGroupManager.getGroupsForWorld(deathWorld)) {
            profile = worldGroup.getGroupProfileContainer().getPlayerProfileNow(event.getEntity());
            resetStatsOnDeath(event, profile);
        }
        Logging.finer("=== Finished handling PlayerDeathEvent for: " + event.getEntity().getName() + "! ===");
    }

    private void resetStatsOnDeath(PlayerDeathEvent event, PlayerProfile profile) {
        profile.set(Sharables.LEVEL, event.getNewLevel());
        profile.set(Sharables.EXPERIENCE, (float) event.getNewExp());
        profile.set(Sharables.TOTAL_EXPERIENCE, event.getNewTotalExp());
        if (config.getResetLastLocationOnDeath()) {
            profile.set(Sharables.LAST_LOCATION, null);
        }
        profileDataSource.updatePlayerProfile(profile);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void playerRespawn(PlayerRespawnEvent event) {
        Location respawnLoc = event.getRespawnLocation();
        if (respawnLoc == null) {
            // This probably only happens if a naughty plugin sets the location to null...
            return;
        }
        final Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(
                inventories,
                () -> verifyCorrectWorld(
                        player,
                        player.getWorld().getName(),
                        FutureNow.get(profileDataSource.getGlobalProfile(GlobalProfileKey.create(player)))),
                2L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    void entityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Item) && !(entity instanceof InventoryHolder)) {
            return;
        }

        World fromWorld = event.getFrom().getWorld();
        if (fromWorld == null) {
            // Apparently this happens sometimes.
            Logging.fine("Entity %s attempted to go from null world", entity);
            return;
        }

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
        fromGroups = fromGroups.stream().filter(it -> it.isSharing(Sharables.INVENTORY)).toList();
        toGroups = toGroups.stream().filter(it -> it.isSharing(Sharables.INVENTORY)).toList();
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
        fromWorldProfileContainer.clearContainerCache();

        List<WorldGroup> fromGroups = worldGroupManager.getGroupsForWorld(unloadWorldName);
        for (WorldGroup fromGroup : fromGroups) {
            fromGroup.getGroupProfileContainer().clearContainerCache();
        }
    }
}
