package org.mvplugins.multiverse.inventories.listeners;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.dumptruckman.minecraft.util.Logging;
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
import org.mvplugins.multiverse.inventories.handleshare.PlayerShareHandlingState;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareWriter;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.RespawnLocation;

import static org.mvplugins.multiverse.inventories.util.MinecraftTools.findAnchorFromRespawnLocation;
import static org.mvplugins.multiverse.inventories.util.MinecraftTools.findBedFromRespawnLocation;

/**
 * Handles player spawn location changes for BED_SPAWN sharable.
 */
@Service
final class SpawnChangeListener implements MVInvListener {

    private final MultiverseInventories inventories;
    private final PlayerShareHandlingState playerShareHandlingState;

    @Inject
    public SpawnChangeListener(MultiverseInventories inventories, PlayerShareHandlingState playerShareHandlingState) {
        this.inventories = inventories;
        this.playerShareHandlingState = playerShareHandlingState;
    }

    @EventClass("com.destroystokyo.paper.event.player.PlayerSetSpawnEvent")
    @DefaultEventPriority(EventPriority.MONITOR)
    EventRunnable<?> onPlayerSetSpawn() {
        return new EventRunnable<PlayerSetSpawnEvent>() {
            @Override
            public void onEvent(PlayerSetSpawnEvent event) {
                Player player = event.getPlayer();
                if (playerShareHandlingState.isHandlingSharable(player, Sharables.BED_SPAWN)) {
                    Logging.finest("Setting new spawn location silently for player %s due to share handling.",
                            player.getName());
                    event.setNotifyPlayer(false);
                    return;
                }
                Location newSpawnLoc = event.getLocation();
                if (newSpawnLoc == null) {
                    updatePlayerSpawn(player, null);
                    return;
                }
                if (event.getCause() == PlayerSetSpawnEvent.Cause.BED) {
                    updatePlayerSpawn(player, new RespawnLocation(
                            findBedFromRespawnLocation(newSpawnLoc),
                            RespawnLocation.RespawnLocationType.BED
                    ));
                    return;
                }
                if (event.getCause() == PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR) {
                    updatePlayerSpawn(player, new RespawnLocation(
                            findAnchorFromRespawnLocation(newSpawnLoc),
                            RespawnLocation.RespawnLocationType.ANCHOR
                    ));
                    return;
                }
                updatePlayerSpawn(player, new RespawnLocation(newSpawnLoc, RespawnLocation.RespawnLocationType.UNKNOWN));
            }
        };
    }

    @SuppressWarnings("removal")
    @EventClass("org.bukkit.event.player.PlayerSpawnChangeEvent")
    @SkipIfEventExist("com.destroystokyo.paper.event.player.PlayerSetSpawnEvent")
    @DefaultEventPriority(EventPriority.MONITOR)
    EventRunnable<?> onPlayerSpawnChange() {
        return new EventRunnable<PlayerSpawnChangeEvent>() {
            @Override
            public void onEvent(PlayerSpawnChangeEvent event) {
                Player player = event.getPlayer();
                if (playerShareHandlingState.isHandlingSharable(player, Sharables.BED_SPAWN)) {
                    return;
                }
                Location newSpawnLoc = event.getNewSpawn();
                if (event.getCause() == PlayerSpawnChangeEvent.Cause.BED) {
                    updatePlayerSpawn(player, new RespawnLocation(
                            findBedFromRespawnLocation(newSpawnLoc),
                            RespawnLocation.RespawnLocationType.BED
                    ));
                    return;
                }
                if (event.getCause() == PlayerSpawnChangeEvent.Cause.RESPAWN_ANCHOR) {
                    updatePlayerSpawn(player, new RespawnLocation(
                            findAnchorFromRespawnLocation(newSpawnLoc),
                            RespawnLocation.RespawnLocationType.ANCHOR
                    ));
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
