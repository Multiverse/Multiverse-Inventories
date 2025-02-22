package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent.Cause;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.share.Sharables;

import javax.annotation.Nullable;

import static org.mvplugins.multiverse.inventories.util.MinecraftTools.findAnchorFromRespawnLocation;
import static org.mvplugins.multiverse.inventories.util.MinecraftTools.findBedFromRespawnLocation;

/**
 * Handles player spawn location changes for BED_SPAWN sharable.
 */
public final class SpawnChangeListener implements Listener {

    private final MultiverseInventories inventories;

    public SpawnChangeListener(MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onSpawnChange(PlayerSpawnChangeEvent event) {
        Player player = event.getPlayer();

        Logging.fine("Respawn cause: %s", event.getCause());

        if (event.getCause() == Cause.BED) {
            updatePlayerSpawn(player, findBedFromRespawnLocation(event.getNewSpawn()));
            return;
        }
        if (event.getCause() == Cause.RESPAWN_ANCHOR) {
            updatePlayerSpawn(player, findAnchorFromRespawnLocation(event.getNewSpawn()));
            return;
        }
        updatePlayerSpawn(player, event.getNewSpawn());
    }

    private void updatePlayerSpawn(Player player, Location location) {
        SingleShareWriter.of(this.inventories, player, Sharables.BED_SPAWN)
                .write(location == null ? null : location.clone(), true);
    }
}
