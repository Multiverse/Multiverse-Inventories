package org.mvplugins.multiverse.inventories.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.EventRunnable;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventClass;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.SkipIfEventExist;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareWriter;
import org.mvplugins.multiverse.inventories.share.Sharables;

import static org.mvplugins.multiverse.inventories.util.MinecraftTools.findAnchorFromRespawnLocation;
import static org.mvplugins.multiverse.inventories.util.MinecraftTools.findBedFromRespawnLocation;

/**
 * Handles player spawn location changes for BED_SPAWN sharable.
 */
@Service
final class SpawnChangeListener implements MVInvListener {

    private final MultiverseInventories inventories;

    @Inject
    public SpawnChangeListener(MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    @EventClass("com.destroystokyo.paper.event.player.PlayerSetSpawnEvent")
    @DefaultEventPriority(EventPriority.MONITOR)
    EventRunnable onPlayerSetSpawn() {
        return new EventRunnable<PlayerSetSpawnEvent>() {
            @Override
            public void onEvent(PlayerSetSpawnEvent event) {
                if (Sharables.isIgnoringSpawnListener(event.getPlayer())) {
                    return;
                }
                Player player = event.getPlayer();
                if (event.getCause() == PlayerSetSpawnEvent.Cause.BED) {
                    updatePlayerSpawn(player, findBedFromRespawnLocation(event.getLocation()));
                    return;
                }
                if (event.getCause() == PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR) {
                    updatePlayerSpawn(player, findAnchorFromRespawnLocation(event.getLocation()));
                    return;
                }
                updatePlayerSpawn(player, event.getLocation());
            }
        };
    }

    @EventClass("org.bukkit.event.player.PlayerSpawnChangeEvent")
    @SkipIfEventExist("com.destroystokyo.paper.event.player.PlayerSetSpawnEvent")
    @DefaultEventPriority(EventPriority.MONITOR)
    EventRunnable onPlayerSpawnChange() {
        return new EventRunnable<PlayerSpawnChangeEvent>() {
            @Override
            public void onEvent(PlayerSpawnChangeEvent event) {
                if (Sharables.isIgnoringSpawnListener(event.getPlayer())) {
                    return;
                }
                Player player = event.getPlayer();
                if (event.getCause() == PlayerSpawnChangeEvent.Cause.BED) {
                    updatePlayerSpawn(player, findBedFromRespawnLocation(event.getNewSpawn()));
                    return;
                }
                if (event.getCause() == PlayerSpawnChangeEvent.Cause.RESPAWN_ANCHOR) {
                    updatePlayerSpawn(player, findAnchorFromRespawnLocation(event.getNewSpawn()));
                    return;
                }
                updatePlayerSpawn(player, event.getNewSpawn());
            }
        };
    }

    private void updatePlayerSpawn(Player player, Location location) {
        SingleShareWriter.of(this.inventories, player, Sharables.BED_SPAWN)
                .write(location == null ? null : location.clone(), true);
    }
}
