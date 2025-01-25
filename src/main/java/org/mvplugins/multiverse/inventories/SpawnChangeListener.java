package org.mvplugins.multiverse.inventories;

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
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.share.Sharables;

import javax.annotation.Nullable;
import java.util.List;

public class SpawnChangeListener implements Listener {

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
        SingleShareWriter.of(this.inventories, player, Sharables.BED_SPAWN).write(location);
    }

    public static @Nullable Location findBedFromRespawnLocation(@Nullable Location respawnLocation) {
        if (respawnLocation == null) {
            return null;
        }
        var bedSpawnBlock = respawnLocation.getBlock();
        for(int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    var newBedBlock = bedSpawnBlock.getRelative(x, y, z);
                    Logging.finest("Finding bed at: " + newBedBlock);
                    if (newBedBlock.getBlockData() instanceof Bed) {
                        Logging.finer("Found bed!");
                        return newBedBlock.getLocation();
                    }
                }
            }
        }
        Logging.warning("Unable to anchor, respawn may not work as expected!");
        return respawnLocation;
    }

    public static @Nullable Location findAnchorFromRespawnLocation(@Nullable Location respawnLocation) {
        if (respawnLocation == null) {
            return null;
        }
        var bedSpawnBlock = respawnLocation.getBlock();
        for(int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    var newBedBlock = bedSpawnBlock.getRelative(x, y, z);
                    Logging.finest("Finding anchor at: " + newBedBlock);
                    if (newBedBlock.getBlockData() instanceof RespawnAnchor) {
                        Logging.finer("Found anchor!");
                        return newBedBlock.getLocation();
                    }
                }
            }
        }
        Logging.warning("Unable to anchor, respawn may not work as expected!");
        return respawnLocation;
    }
}
